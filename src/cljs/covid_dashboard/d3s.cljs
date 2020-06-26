(ns covid-dashboard.d3s
  "d3.js visualizations and wrapper components"
  (:require
   [cljsjs.d3 :as d3]
   [d3-geo-projection :as d3-geo-projection]
   [d3-geo :as d3-geo]
   [goog.string :as gstring]
   [goog.string.format]
   [reagent.core :as reagent]
   [tupelo.core :refer [spyx]]
   [topojson-client :as topo]))

(def files-covid ["https://covid-dashboard.sunflowerseastar.com/data/06-14-2020.csv" "https://covid-dashboard.sunflowerseastar.com/data/counties-albers-10m.json"])

(defn bubble-map-covid [svg-el-id]
  (-> (.all js/Promise [(.csv js/d3 (first files-covid) #(vector (.-FIPS %) (.-Confirmed %))) (.json js/d3 (second files-covid))])
      (.then
       (fn [[population-data counties-albers-10m-data]]
         (let [myGeoPath (d3-geo/geoPath)
               population-data-map (js/Map.)
               filtered-population-data (->> population-data (filter #(->> % first empty? not)))
               _ (dorun (map #(.set population-data-map (->> % first (gstring/format "%05d")) (second %)) filtered-population-data))
               svg (.. js/d3 (select (str "#" svg-el-id)))

               ;; TODO change hard-coded 1000000 to high-end of domain of populationDataMap values
               scale-radius #((.scaleSqrt js/d3 (clj->js [0 1000]) (clj->js [0 7])) (or % 0))]

           (-> svg
               (.append "path")
               (.datum (topo/feature counties-albers-10m-data (-> counties-albers-10m-data .-objects .-nation)))
               (.attr "fill", "#f3f3f3")
               (.attr "d" myGeoPath))

           (-> svg
               (.append "path")
               (.datum (topo/mesh counties-albers-10m-data (-> counties-albers-10m-data .-objects .-states) (fn [a b] (not= a b))))
               (.attr "fill" "none")
               (.attr "stroke" "#fff")
               (.attr "stroke-linejoin" "round")
               (.attr "d" myGeoPath))

           ;; const t = svg.transition().duration(750);
           ;; (def t (-> svg .transition (.duration 750)))

           ;; marks
           (-> svg
               (.append "g")
               (.attr "fill" "#ff8c94")
               (.attr "fill-opacity" 0.5)
               (.attr "stroke" "#fff")
               (.attr "stroke-width" 0.5)

               (.selectAll "circle")
               (.data (->> (topo/feature counties-albers-10m-data (-> counties-albers-10m-data .-objects .-counties))
                           (.-features)
                           (js->clj)
                           (map #(assoc % :value (.get population-data-map (get % "id"))))
                           (sort-by :value)
                           (clj->js)))
               (.join
                (fn [enter]
                  (-> enter
                      (.append "circle")
                      (.attr "transform" #(str "translate(" (.centroid myGeoPath %) ")"))
                      (.attr "r" #(scale-radius (.-value %)))))
                (fn [update] update)
                (fn [exit]
                  (-> exit
                      (.call (fn [circle]
                               (-> circle
                                   (.transition (-> svg .transition (.duration 750)))
                                   .remove)
                               )))
                  (.remove exit)
                  )
                )

               ))))))

(defn bubble-map-covid-us-d3 []
  (let [svg-el-id "bubble-map-covid-us-d3-svg-root"]
    (reagent/create-class
     {:display-name "bubble-map-covid-us-d3-component"
      :component-did-mount (fn [this] (bubble-map-covid svg-el-id))
      :reagent-render (fn [this] [:svg {:id svg-el-id :class "svg-container" :viewBox [0 0 985 630]}])})))








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

(defn world-bubble-map [svg-el-id height world-bubble-map-data]
  (let [
        margin 30

        format-value (.format js/d3 ".0s")

        data (->> world-bubble-map-data
                  (map (fn [d] {(first d) (js/parseFloat (second d))}))
                  clj->js)

        svg (-> js/d3 (.select (str "#" svg-el-id)))
        width (-> (.node svg) (.-clientWidth))

        ]
    ;; (spyx data)

    (-> (.json js/d3 "https://covid-dashboard.sunflowerseastar.com/data/countries-50m.json" #(vector (.-FIPS %) (.-Confirmed %)))
        (.then
         (fn [world]
           (let [countries (topo/feature world (-> world .-objects .-countries))
                 projection (d3-geo/geoEqualEarth)
                 path (d3-geo/geoPath projection)
                 outline (clj->js {"type" "Sphere"})

                 svg (-> js/d3 (.select (str "#" svg-el-id)))
                 width (-> (.node svg) (.-clientWidth))
                 height 500

                 g (-> svg (.append "g"))

                 ]

             (-> g
                 (.append "g")
                 (.selectAll "path")
                 (.data (.-features countries))
                 (.join "path")
                 (.call (fn [d] (.log js/console d)))
                 (.attr "fill" "black")
                 (.attr "d" path))

             ))))))

(defn world-bubble-map-d3 [line-chart-data]
  (let [height 200 svg-el-id "world-bubble-map-root-svg"]
    (reagent/create-class
     {:display-name "world-bubble-map-d3"
      :reagent-render (fn [this] [:svg {:id svg-el-id :class "svg-container" :viewBox [0 0 300 height]}])
      :component-did-mount #(world-bubble-map svg-el-id height line-chart-data)})))
