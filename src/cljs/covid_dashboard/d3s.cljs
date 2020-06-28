(ns covid-dashboard.d3s
  "d3.js visualizations and wrapper components"
  (:require
   [cljsjs.d3 :as d3]
   [breaking-point.core :as bp]
   [covid-dashboard.subs :as subs]
   [d3-geo-projection :as d3-geo-projection]
   [d3-geo :as d3-geo]
   [goog.string :as gstring]
   [goog.string.format]
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [tupelo.core :refer [spyx]]
   [topojson-client :as topo]))

(def files-covid ["https://covid-dashboard.sunflowerseastar.com/data/06-14-2020.csv" "https://covid-dashboard.sunflowerseastar.com/data/counties-albers-10m.json"])

(defn bubble-map-covid [svg-el-id width height]
  (-> (.all js/Promise [(.csv js/d3 (first files-covid) #(vector (.-FIPS %) (.-Confirmed %))) (.json js/d3 (second files-covid))])
      (.then
       (fn [[csse-daily-report counties-albers-10m-data]]
         (let [geojson (topo/feature counties-albers-10m-data (-> counties-albers-10m-data .-objects .-nation))
               projection (-> (.geoIdentity js/d3)
                              (.fitExtent (clj->js [[0 0] [(- width 50) (- height 50)]]) geojson))

               path (-> (d3-geo/geoPath) (.projection projection))
               data-map (js/Map.)
               filtered-csse-daily-report (->> csse-daily-report (filter #(->> % first empty? not)))
               _ (dorun (map #(.set data-map (->> % first (gstring/format "%05d")) (second %)) filtered-csse-daily-report))

               svg (.. js/d3 (select (str "#" svg-el-id)))
               g (-> svg (.append "g"))

               data-map-values (->> (.values data-map) (map js/parseInt) clj->js)
               scale-radius (.scaleSqrt js/d3 (clj->js (.extent js/d3 (.extent js/d3 data-map-values))) (clj->js [1 40]))

               scale-extent (clj->js [1 9])
               zoom-k->scaled-zoom-k (.scaleLinear js/d3 scale-extent (clj->js [1 0.3]))

               zoomed #(let [transform (-> js/d3 .-event .-transform)
                             k (.-k transform)
                             circles (.selectAll g ".g-circles circle")]
                         (do
                           (-> circles
                               (.attr "r" (fn [d]
                                            (let [scaled-radius (* (zoom-k->scaled-zoom-k k)
                                                                   (scale-radius (.-value d)))]
                                              (if (js/isNaN scaled-radius) 0 scaled-radius)))))
                           (-> (.select g ".g-circles")
                               (.attr "stroke-width" (* (zoom-k->scaled-zoom-k k) 0.5)))
                           (-> g (.attr "transform" transform)
                               (.attr "stroke-width" (/ 1 k)))))
               my-zoom (-> (.zoom js/d3)
                           (.scaleExtent scale-extent)
                           (.on "zoom" zoomed))]

           (-> svg (.attr "viewBox" (clj->js [0 0 width height])))

           (-> g
               (.append "path")
               (.datum (topo/feature counties-albers-10m-data (-> counties-albers-10m-data .-objects .-nation)))
               (.attr "fill", "#f3f3f3")
               (.attr "d" path))

           (-> g
               (.append "path")
               (.datum (topo/mesh counties-albers-10m-data (-> counties-albers-10m-data .-objects .-states) (fn [a b] (not= a b))))
               (.attr "fill" "none")
               (.attr "stroke" "#fff")
               (.attr "stroke-linejoin" "round")
               (.attr "d" path))

           ;; marks
           (-> g
               (.append "g")
               (.attr "class" "g-circles")
               (.attr "fill" "#ff8c94")
               (.attr "fill-opacity" 0.5)
               (.attr "stroke" "#fff")
               (.attr "stroke-width" 0.5)

               (.selectAll "circle")
               (.data (->> (topo/feature counties-albers-10m-data (-> counties-albers-10m-data .-objects .-counties))
                           (.-features)
                           (js->clj)
                           (map #(assoc % :value (.get data-map (get % "id"))))
                           (sort-by :value)
                           (clj->js)))
               (.join
                (fn [enter]
                  (-> enter
                      (.append "circle")
                      (.attr "transform" #(str "translate(" (.centroid path %) ")"))
                      (.attr "r" #(scale-radius (.-value %)))))))

           (-> svg (.call my-zoom))

           )))))

(defn bubble-map-covid-us-d3 []
  (let [width @(re-frame/subscribe [::bp/screen-width])
        height @(re-frame/subscribe [::bp/screen-height])
        svg-el-id "bubble-map-covid-us-d3-svg-root"]
    (reagent/create-class
     {:display-name "bubble-map-covid-us-d3-component"
      :component-did-mount (fn [this] (bubble-map-covid svg-el-id width height))
      :reagent-render (fn [this] [:svg {:id svg-el-id :class "svg-container"}])})))








(defn parse-date [x] ((.timeParse js/d3 "%m/%d/%Y") x))


(defn line-chart [svg-el-id height line-chart-data]
  (let [
        margin 30

        format-value (.format js/d3 ".0s")

        data (->> line-chart-data
                  (map (fn [d] {:date (parse-date (first d)) :value (js/parseFloat (second d))}))
                  clj->js)

        svg (-> js/d3 (.select (str "#" svg-el-id)))
        width (-> (.node svg) (.-clientWidth))

        x-scale (-> (.scaleUtc js/d3)
                    (.domain (.extent js/d3 data (fn [d] (.-date d))))
                    (.range (clj->js [(* margin 1.3) (- width 20)])))

        y-scale (-> (.scaleLinear js/d3)
                    (.domain (clj->js [0 (.max js/d3 data (fn [d] (.-value d)))]))
                    (.nice)
                    (.range (clj->js [(- height (* margin 1.3)) 0])))

        my-line (-> (.line js/d3)
                    (.defined (fn [d] (not (js/isNaN (.-value d)))))
                    (.x (fn [d] (x-scale (.-date d))))
                    (.y (fn [d] (y-scale (.-value d)))))

        x-axis (fn [g] (-> (.attr g "transform" (str "translate(0," (- height 20) ")"))
                           (.call (-> (.axisBottom js/d3 x-scale)
                                      (.ticks (/ width 80))
                                      (.tickSizeOuter 0)))
                           (.call (fn [g] (-> (.select g ".domain") (.remove))))))

        y-axis (fn [g] (-> (.attr g "transform" (str "translate(" margin ",0)"))
                           (.call (-> (.axisLeft js/d3 y-scale)
                                      (.ticks 5)
                                      (.tickFormat format-value)))
                           (.call (fn [g] (-> (.select g ".domain") (.remove))))
                           (.call (fn [g] (-> (.select g ".tick:last-of-type text")
                                              (.clone)
                                              (.attr "x" 6)
                                              (.attr "text-anchor" "start")
                                              (.text "linear"))))))]

    (-> svg (.attr "viewBox" (clj->js [0 0 (-> (.node svg) (.-clientWidth)) height])))

    (-> svg (.append "g")
        (.call x-axis))

    (-> svg (.append "g")
        (.call y-axis))

    (-> svg (.append "path")
        (.datum data)
        (.attr "fill" "none")
        (.attr "stroke" "#bbb")
        (.attr "stroke-width" 1.5)
        (.attr "stroke-linejoin" "round")
        (.attr "stroke-linecap" "round")
        (.attr "d" my-line))))

(defn line-chart-d3 [line-chart-data]
  (let [height 200 svg-el-id "line-chart-root-svg"]
    (reagent/create-class
     {:display-name "line-chart-d3"
      :reagent-render (fn [this] [:svg {:id svg-el-id :class "svg-container" :viewBox [0 0 300 height]}])
      :component-did-mount #(line-chart svg-el-id height line-chart-data)})))












(defn line-chart-log [svg-el-id height line-chart-data]
  (let [
        margin 30

        format-value (.format js/d3 ".0s")

        data (->> line-chart-data
                  (map (fn [d] {:date (parse-date (first d)) :value (js/parseFloat (second d))}))
                  clj->js)

        svg (-> js/d3 (.select (str "#" svg-el-id)))
        width (-> (.node svg) (.-clientWidth))

        x-scale (-> (.scaleUtc js/d3)
                    (.domain (.extent js/d3 data (fn [d] (.-date d))))
                    (.range (clj->js [(* margin 1.3) (- width 20)])))

        y-scale (-> (.scaleLog js/d3)
                    (.domain (clj->js [1000 (.max js/d3 data (fn [d] (.-value d)))]))
                    (.nice)
                    (.range (clj->js [(- height (* margin 1.3)) 8])))

        my-line (-> (.line js/d3)
                    (.defined (fn [d] (not (js/isNaN (.-value d)))))
                    (.x (fn [d] (x-scale (.-date d))))
                    (.y (fn [d] (y-scale (.-value d)))))

        x-axis (fn [g] (-> (.attr g "transform" (str "translate(0," (- height 20) ")"))
                           (.call (-> (.axisBottom js/d3 x-scale)
                                      (.ticks 3)
                                      (.tickSizeOuter 0)))
                           (.call (fn [g] (-> (.select g ".domain") (.remove))))))

        y-axis (fn [g] (-> (.attr g "transform" (str "translate(" margin ",9)"))
                           (.call (-> (.axisLeft js/d3 y-scale)
                                      (.ticks 3)
                                      (.tickFormat format-value)))
                           (.call (fn [g] (-> (.select g ".domain") (.remove))))
                           (.call (fn [g] (-> (.select g ".tick:last-of-type text")
                                              (.clone)
                                              (.attr "x" 6)
                                              (.attr "text-anchor" "start")
                                              (.text "logarithmic"))))))]

    (-> svg (.attr "viewBox" (clj->js [0 0 (-> (.node svg) (.-clientWidth)) height])))

    (-> svg (.append "g")
        (.call x-axis))

    (-> svg (.append "g")
        (.call y-axis))

    (-> svg (.append "path")
        (.datum data)
        (.attr "fill" "none")
        (.attr "stroke" "#bbb")
        (.attr "stroke-width" 1.5)
        (.attr "stroke-linejoin" "round")
        (.attr "stroke-linecap" "round")
        (.attr "d" my-line))))

(defn line-chart-log-d3 [line-chart-data]
  (let [height 200 svg-el-id "line-chart-log-root-svg"]
    (reagent/create-class
     {:display-name "line-chart-log-d3"
      :reagent-render (fn [this] [:svg {:id svg-el-id :class "svg-container" :viewBox [0 0 300 height]}])
      :component-did-mount #(line-chart-log svg-el-id height line-chart-data)})))




(def cross-name-map {"US" "United States of America"})
(defn get-name [name]
  (or (get cross-name-map name) name))

(defn world-bubble-map [svg-el-id width height world-bubble-map-data]
  (let [data (->> world-bubble-map-data
                  (reduce (fn [acc d] (assoc acc (get-name (first d)) (second d))) {})
                  clj->js)
        svg (-> js/d3 (.select (str "#" svg-el-id)))]
    (-> (.json js/d3 "https://covid-dashboard.sunflowerseastar.com/data/countries-50m.json" #(vector (.-FIPS %) (.-Confirmed %)))
        (.then
         (fn [world]
           (let [countries (topo/feature world (-> world .-objects .-countries))
                 projection (-> (d3-geo/geoNaturalEarth1)
                                (.fitExtent (clj->js [[0.5 0.5] [(- width 0.5) (- height 0.5)]]), (clj->js {"type" "Sphere"}))
                                (.precision 0.1))
                 path (d3-geo/geoPath projection)
                 outline (clj->js {"type" "Sphere"})
                 g (-> svg (.append "g"))
                 radius (.scaleSqrt js/d3 (.extent js/d3 (.values js/Object data)) (clj->js [3 20]))

                 scale-extent (clj->js [1 9])
                 zoom-k->scaled-zoom-k (.scaleSqrt js/d3 scale-extent (clj->js [1 0.22]))

                 zoomed #(let [transform (-> js/d3 .-event .-transform)
                               k (.-k transform)
                               circles (.selectAll g ".g-circles circle")]
                           (do (-> circles
                                   (.attr "r" (fn [d]
                                                (let [scaled-radius (* (zoom-k->scaled-zoom-k k)
                                                                       (radius (aget data (-> d .-properties .-name))))]
                                                  (if (js/isNaN scaled-radius) 0 scaled-radius)))))
                               (-> (.select g ".g-circles")
                                   (.attr "stroke-width" (* (zoom-k->scaled-zoom-k k) 0.5)))
                               (-> g (.attr "transform" transform)
                                   (.attr "stroke-width" (/ 1 k)))))
                 my-zoom (-> (.zoom js/d3)
                             (.scaleExtent scale-extent)
                             (.on "zoom" zoomed))

                 graticule-outline (-> (d3-geo/geoGraticule) (.outline))
                 graticule (-> (d3-geo/geoGraticule10))]

             (-> svg (.attr "viewBox" (clj->js [0 0 width height])))

             ;; land
             (-> g
                 (.append "g")
                 (.selectAll "path")
                 (.data (.-features countries))
                 (.join "path")
                 (.attr "fill", "#f3f3f3")
                 (.attr "d" path)
                 (.append "title")
                 (.text (fn [d] (aget data (-> d .-properties .-name)))))


             (-> g
                 (.append "path")
                 (.datum graticule-outline)
                 (.attr "class" "graticule")
                 (.attr "d" path))

             (-> g
                 (.append "path")
                 (.datum graticule)
                 (.attr "class" "graticule")
                 (.attr "d" path))

             ;; country borders
             (-> g
                 (.append "path")
                 (.datum (topo/mesh world (-> world .-objects .-countries) (fn [a b] (not= a b))))
                 (.attr "fill" "none")
                 (.attr "stroke" "#fff")
                 (.attr "stroke-linejoin" "round")
                 (.attr "d" path))

             ;; marks
             (-> g
                 (.append "g")
                 (.attr "class" "g-circles")
                 (.attr "fill" "#ff8c94")
                 (.attr "fill-opacity" 0.5)
                 (.attr "stroke" "#fff")
                 (.attr "stroke-width" 0.5)
                 (.selectAll "circle")
                 (.data (.-features countries) #(.-id %))
                 (.join (fn [enter]
                          (-> enter
                              (.append "circle")
                              (.attr "transform" (fn [d] (str "translate(" (.centroid path d) ")")))
                              (.attr "r" 0))))
                 (.transition)
                 (.duration 2000)
                 (.attr "r" (fn [d] (radius (aget data (-> d .-properties .-name))))))

             (-> svg (.call my-zoom))

             ))))))

(defn world-bubble-map-d3 [line-chart-data]
  (let [
        width @(re-frame/subscribe [::bp/screen-width])
        height @(re-frame/subscribe [::bp/screen-height])
        svg-el-id "world-bubble-map-root-svg"]
    (reagent/create-class
     {:display-name "world-bubble-map-d3"
      :component-did-mount #(world-bubble-map svg-el-id width height line-chart-data)
      :reagent-render (fn [this] [:svg {:id svg-el-id :class "svg-container"}])
      })))
