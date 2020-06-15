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

(defn format [x] (.format js/d3 ",.0f" x))

(def radius-data )

(defn bubble-map [starting-width]
  (println "bubble-map" starting-width)
  (-> (.json js/d3 "https://covid-dashboard.sunflowerseastar.com/data/population.json")
      (.then
       (fn [population-data]
         (-> (.json js/d3  "https://covid-dashboard.sunflowerseastar.com/data/counties-albers-10m.json")
             (.then
              (fn [counties-albers-10m-data]
                ;; (println counties-albers-10m-data)
                (let [myGeoPath (geoPath)
                      population-data-map (js/Map.)
                      _ (dorun (map #(.set population-data-map (first %) (second %)) (js->clj population-data)))
                      svg (.. js/d3 (select "#bubble-map-d3-svg-root") (attr "width" starting-width) (attr "height" (* starting-width 0.6)))

                      ;; TODO change hard-coded 1000000 to high-end of domain of populationDataMap values
                      scale-radius #((.scaleSqrt js/d3 (clj->js [0 1000000]) (clj->js [0 10])) (or % 0))
                      ]

                  (do
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

                    ;; legend setup
                    (-> svg
                        (.append "g")
                        (.attr "fill" "#777")
                        (.attr "transform" (str "translate(50 100)"))
                        (.attr "text-anchor" "middle")
                        (.style "font" "10px sans-serif")
                        (.selectAll "g")
                        (.data (clj->js [1000000 5000000 10000000]))
                        (.join "g")
                        ;; (.call (fn [d] (do (.log js/console d))))
                        (.append "circle")
                        (.attr "fill" "none")
                        (.attr "stroke" "#f00")
                        (.attr "cy" #(* -1 (scale-radius %)))
                        (.attr "r" scale-radius))

                    ;; legend text
                    ;; (-> svg
                    ;;     (.append "text")
                    ;;     (.attr "y" (fn [d] (* -2 (scale-radius d))))
                    ;;     (.attr "dy" "1.3em")
                    ;;     (.text (format ".1s")))

                    ;; marks
                    (-> svg
                        (.append "g")

                        (.attr "fill" "purple")
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
                        (.attr "r" #(scale-radius (.-value %)))

                        ;; TODO title
                        ;; (.append "title")
                        ;; (.text (fn [d] (str (-> d .-properties .-name) " " (format (.-value d)))))
                        ))))))))))

(defn bubble-map-d3 []
  (r/create-class
   {:display-name "bubble-map-d3"
    :reagent-render (fn [this] [:div#d3-bubble-map-container [:svg#bubble-map-d3-svg-root {:viewBox [0 0 975 610]} [:g.hello]]])
    :component-did-mount #(bubble-map @(re-frame/subscribe [::bp/screen-width]))}))
