(ns covid-dashboard.d3s
  (:require
   [d3-geo :refer [geoPath]]
   [topojson-client :refer [feature mesh]]
   [breaking-point.core :as bp]
   [re-frame.core :as re-frame]
   [reagent.core :as r]
   [cljsjs.d3 :as d3]
   [tupelo.core :refer [it-> spyx]]
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
               scale-radius #((.scaleSqrt js/d3 (clj->js [0 1000]) (clj->js [0 10])) (or % 0))]

           (-> svg
               (.attr "width" 975)
               (.attr "height" 610))

           (-> svg
               (.append "path")
               (.datum (feature counties-albers-10m-data (-> counties-albers-10m-data .-objects .-nation)))
               (.attr "fill", "#ccc")
               (.attr "d" myGeoPath))

           (-> svg
               (.append "path")
               (.datum (mesh counties-albers-10m-data (-> counties-albers-10m-data .-objects .-states) (fn [a b] (not= a b))))
               (.attr "fill" "none")
               (.attr "stroke" "white")
               (.attr "stroke-linejoin" "round")
               (.attr "d" myGeoPath))

           ;; marks
           (-> svg
               (.append "g")

               (.attr "fill" "blue")
               (.attr "fill-opacity" 0.2)
               (.attr "stroke" "#fff")
               (.attr "stroke-width" 0.5)

               (.selectAll "circle")
               (.data (->> (feature counties-albers-10m-data (-> counties-albers-10m-data .-objects .-counties))
                           (.-features)
                           (js->clj)
                           (map #(assoc % :value (.get population-data-map (get % "id"))))
                           (sort-by :value)
                           (clj->js)))
               (.join "circle")
               (.attr "transform" #(str "translate(" (.centroid myGeoPath %) ")"))
               (.attr "r" #(scale-radius (.-value %)))))))))

(defn bubble-map-covid-us-d3 []
  (let [svg-el-id "bubble-map-covid-us-d3-svg-root"]
    (r/create-class
     {:display-name "bubble-map-covid-us-d3"
      :reagent-render (fn [this] [:svg {:id svg-el-id :viewBox [0 0 975 610]} [:g.hello]])
      :component-did-mount (fn [this] (bubble-map-covid svg-el-id))})))
