(ns covid-dashboard.d3s
  (:require
   [d3-geo :refer [geoPath]]
   [topojson-client :refer [feature mesh]]
   [cljsjs.d3 :as d3]
   [reagent.core :refer [create-class]]
   [goog.string :as gstring]
   [goog.string.format]))

(def files-covid ["https://covid-dashboard.sunflowerseastar.com/data/06-14-2020.csv" "https://covid-dashboard.sunflowerseastar.com/data/counties-albers-10m.json"])

(defn bubble-map-covid [svg-el-id]
  (-> (.all js/Promise [(.csv js/d3 (first files-covid) #(vector (.-FIPS %) (.-Confirmed %))) (.json js/d3 (second files-covid))])
      (.then
       (fn [[population-data counties-albers-10m-data]]
         (let [myGeoPath (geoPath)
               population-data-map (js/Map.)
               filtered-population-data (->> population-data (filter #(->> % first empty? not)))
               _ (dorun (map #(.set population-data-map (->> % first (gstring/format "%05d")) (second %)) filtered-population-data))
               svg (.. js/d3 (select (str "#" svg-el-id)))

               ;; TODO change hard-coded 1000000 to high-end of domain of populationDataMap values
               scale-radius #((.scaleSqrt js/d3 (clj->js [0 1000]) (clj->js [0 7])) (or % 0))]

           (-> svg
               (.append "path")
               (.datum (feature counties-albers-10m-data (-> counties-albers-10m-data .-objects .-nation)))
               (.attr "fill", "#f3f3f3")
               (.attr "d" myGeoPath))

           (-> svg
               (.append "path")
               (.datum (mesh counties-albers-10m-data (-> counties-albers-10m-data .-objects .-states) (fn [a b] (not= a b))))
               (.attr "fill" "none")
               (.attr "stroke" "#fff")
               (.attr "stroke-linejoin" "round")
               (.attr "d" myGeoPath))

           ;; marks
           (-> svg
               (.append "g")
               (.attr "fill" "#ff8c94")
               (.attr "fill-opacity" 0.5)
               (.attr "stroke" "#fff")
               (.attr "stroke-width" 0.5)

               (.selectAll "circle")
               (.data (->> (feature counties-albers-10m-data (-> counties-albers-10m-data .-objects .-counties))
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
                (fn [exit] (.remove exit)))))))))

(defn bubble-map-covid-us-d3 []
  (let [svg-el-id "bubble-map-covid-us-d3-svg-root"]
    (create-class
     {:display-name "bubble-map-covid-us-d3-component"
      :component-did-mount (fn [this] (bubble-map-covid svg-el-id))
      :reagent-render (fn [this] [:svg {:id svg-el-id :class "svg-container" :viewBox [0 0 985 630]}])})))
