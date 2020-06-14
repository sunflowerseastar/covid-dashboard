(ns covid-dashboard.d3s
  (:require
   [d3-geo :refer [geoPath]]
   [topojson-client :refer [feature mesh]]
   [breaking-point.core :as bp]
   [re-frame.core :as re-frame]
   [reagent.core :as r]
   [cljsjs.d3 :as d3]
   [tupelo.core :refer [it-> spyx]]))

(def margin {:top 100, :right 100, :bottom 100, :left 100})

(defn get-scales [width height]
  [(.. js/d3 scaleTime (range #js [0 width]))
   (.. js/d3 scaleLinear (range #js [height 0]))])

(defn get-line [x y]
  (.. js/d3 line
      (x #(x (.-date %)))
      (y #(y (.-close %)))))

(defn get-svg [margin width height]
  (.. js/d3 (select "#d3-line-chart-container svg")
      (attr "width" (+ width (:left margin) (:right margin)))
      (attr "height" (+ height (:top margin) (:bottom margin)))
      (append "g")
      (attr "transform" (str "translate(" (:left margin) \, (:top margin) \) ))))

(defn set-domains [x y data]
  (.domain x (.extent js/d3 data #(.-date %)))
  (.domain y (.extent js/d3 data #(.-close %))))

(defn add-line [svg line data]
  (.. svg (append "path")
      (datum data)
      (attr "class" "line")
      (attr "d" line)))

(defn ibm-stock [starting-width]
  (let [width (- starting-width (:left margin) (:right margin))
        height (* starting-width 0.6)
        parse-date (.timeParse js/d3 "%d-%b-%y")
        [x y] (get-scales width height)
        line (get-line x y)
        svg (get-svg margin width height)]
    (-> (.csv js/d3 "https://covid-dashboard.sunflowerseastar.com/data/ibm.csv")
        (.then (fn [ibm-data-response]
                 (let [data (->> ibm-data-response js->clj
                                 (map (fn [d]
                                        {:date (parse-date (get d "date")) :close (js/parseFloat (get d "close"))})
                                      ) clj->js)]
                   (set-domains x y data)
                   (.. js/d3 (select ".x.axis")
                       (call (.axisBottom js/d3 x)))
                   (.. js/d3 (select ".y.axis")
                       (call (.axisLeft js/d3 y)))
                   (add-line svg line data))))
        (.catch #(js/console.log %)))))

(defn line-chart-d3 []
  (r/create-class
   {:display-name "line-chart-d3"
    :reagent-render (fn [this] [:div#d3-line-chart-container [:svg [:g.graph]]])
    :component-did-mount #(ibm-stock (/ @(re-frame/subscribe [::bp/screen-width]) 3))}))

(defn svg-with-width-and-height [el-id width height]
  (.. js/d3 (select (str "#" el-id " svg"))
      (attr "width" width)
      (attr "height" height)
      (append "g")))

(defn format [x] (.format js/d3 ",.0f" x))

(defn bubble-map [starting-width]
  (println "bubble-map")
  (-> (.json js/d3 "https://covid-dashboard.sunflowerseastar.com/data/population.json")
      (.then
       (fn [population-data]
         (-> (.json js/d3  "https://covid-dashboard.sunflowerseastar.com/data/counties-albers-10m.json")
             (.then
              (fn [us]
                ;; (println us)
                (let [myGeoPath (geoPath)
                      data (js/Map.)
                      _ (dorun (map #(.set data (first %) (second %)) (js->clj population-data)))
                      svg (svg-with-width-and-height "d3-bubble-map-container" starting-width (* starting-width 0.6))
                      radius #(.scaleSqrt js/d3
                                          [0 (.quantile js/d3 (-> (.-values %) (.sort "ascending")) 0.985)]
                                          [0 15])]

                  ;; (spyx (js->clj population-data) (js->clj data))
                  ;; (spyx data)
                  (spyx (.get data "34029"))
                  (do
                    (-> svg
                        (.append "path")
                        (.datum (feature us (-> us .-objects .-nation)))
                        (.attr "fill", "#ccc")
                        (.attr "d" myGeoPath))

                    (-> svg
                        (.append "path")
                        (.datum (mesh us (-> us .-objects .-states) (fn [a b] (not= a b))))
                        (.attr "fill" "none")
                        (.attr "stroke" "white")
                        (.attr "stroke-linejoin" "round")
                        (.attr "d" myGeoPath))

                    ;; legend
                    ;; (-> svg
                    ;;     (.append "g")
                    ;;     (.attr "fill" "#777")
                    ;;     (.attr "transform" "translate(925 608)")
                    ;;     (.attr "text-anchor" "middle")
                    ;;     (.style "font" "10px sans-serif")
                    ;;     (.selectAll "g")
                    ;;     (.data [1e6 5e6 1e7])
                    ;;     (.join "g"))

                    ;; legend
                    ;; (-> svg
                    ;;     (.append "circle")
                    ;;     (.attr "fill" "none")
                    ;;     (.attr "stroke" "#ccc")
                    ;;     (.attr "cy" (fn [d] (* -1 (radius d))))
                    ;;     (.attr "r" radius))

                    ;; legend
                    ;; (-> svg
                    ;;     (.append "text")
                    ;;     (.attr "y" (fn [d] (* -2 (radius d))))
                    ;;     (.attr "dy" "1.3em")
                    ;;     (.text (format ".1s")))

                    (spyx data)
                    (-> svg
                        (.append "g")
                        (.attr "fill" "brown")
                        (.attr "fill-opacity" 0.5)
                        (.attr "stroke" "#fff")
                        (.attr "stroke-width" 0.5)
                        (.selectAll "circle")
                        (.data (it-> (feature us (-> us .-objects .-counties))
                                     (.-features it)
                                     (js->clj it)
                                     (map (fn [x] (assoc x :value (.get data (get x "id")))) it)
                                     (clj->js it)
                                     ;; (js/sort (fn [a b] (- (get b "value")) (get a "value")))
                                     ))
                        (.join "circle")

                        (.attr "transform" (fn [d] (str "translate(" (.centroid myGeoPath d) ")")))
                        (.attr "r" 10)
                        ;; (.attr "r" (fn [d] (do
                        ;;                      (spyx d)
                        ;;                      (radius (get d "value")))))
                        (.append "title")
                        (.text (fn [d] (str (-> d .-properties .-name) " " (format (get d "value")))))))))))))))

(defn bubble-map-d3 []
  (r/create-class
   {:display-name "bubble-map-d3"
    :reagent-render (fn [this] [:div#d3-bubble-map-container [:svg {:viewBox [0 0 975 610]}]])
    :component-did-mount #(bubble-map @(re-frame/subscribe [::bp/screen-width]))}))
