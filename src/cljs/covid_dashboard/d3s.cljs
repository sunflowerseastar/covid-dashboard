(ns covid-dashboard.d3s
  "d3.js visualizations and wrapper components"
  (:require
   [cljsjs.d3 :as d3]
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








(defn parse-date [x] ((.timeParse js/d3 "%d/%m/%Y") x))

(defn line-chart [svg-el-id line-chart-data]
  (let [data (->> line-chart-data js->clj
                  (map (fn [d]
                         {:date (parse-date (get d "Date")) :value (js/parseFloat (get d "Close"))})
                       ) clj->js)

        x-scale (-> (.scaleUtc js/d3)
                    (.domain (.extent js/d3 data (fn [d] (.-date d))))
                    (.range (clj->js [0 300])))

        y-scale (-> (.scaleLinear js/d3)
                    (.domain (clj->js [0 (.max js/d3 data (fn [d] (.-value d)))]))
                    (.range (clj->js [0 200])))

        my-line (-> (.line js/d3)
                    (.defined (fn [d] (do ;; (.log js/console d)
                                        (not (js/isNaN (.-value d))))))
                    (.x (fn [d] (x-scale (.-date d))))
                    (.y (fn [d] (y-scale (.-value d)))))

        svg (.. js/d3 (select (str "#" svg-el-id)))]

    (-> svg (.append "path")
        (.datum data)
        (.attr "fill" "none")
        (.attr "stroke" "steelblue")
        (.attr "stroke-width" 1.5)
        (.attr "stroke-linejoin" "round")
        (.attr "stroke-linecap" "round")
        (.attr "d" my-line))))

(defn line-chart-d3 []
  (let [svg-el-id "line-chart-root-svg"
        line-chart-data (clj->js [{:Date "14/01/2010" :Close 28.33}
                                  {:Date "15/01/2010" :Close 27.86}
                                  {:Date "19/01/2010" :Close 29.09}
                                  {:Date "20/01/2010" :Close 15.64}
                                  {:Date "21/01/2010" :Close 28.15}
                                  {:Date "22/01/2010" :Close 26.75}
                                  {:Date "25/01/2010" :Close 27.47}
                                  {:Date "26/01/2010" :Close 27.86}
                                  {:Date "27/01/2010" :Close 28.12}])]
    (reagent/create-class
     {:display-name "line-chart-d3"
      :reagent-render (fn [this] [:svg {:id svg-el-id :class "svg-container" :viewBox [0 0 300 200]}])
      :component-did-mount #(line-chart svg-el-id line-chart-data)})))
