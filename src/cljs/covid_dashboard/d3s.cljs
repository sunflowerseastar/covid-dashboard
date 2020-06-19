(ns covid-dashboard.d3s
  "d3.js visualizations and wrapper components"
  (:require
   [cljsjs.d3 :as d3]
   [d3-geo :as d3-geo]
   [goog.string :as gstring]
   [goog.string.format]
   [reagent.core :as reagent]
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
                   ;; (set-domains x y data)
                   #_(.. js/d3 (select ".x.axis")
                         (call (.axisBottom js/d3 x)))
                   #_(.. js/d3 (select ".y.axis")
                         (call (.axisLeft js/d3 y)))
                   (add-line svg line data))))
        (.catch #(js/console.log %)))))

(defn line-chart-d3 []
  (reagent/create-class
   {:display-name "line-chart-d3"
    :reagent-render (fn [this] [:div#d3-line-chart-container [:svg [:g.graph]]])
    :component-did-mount #(ibm-stock 500)}))

(defn make-line-chart [svg-el-id]
  (-> (.csv js/d3 "https://covid-dashboard.sunflowerseastar.com/data/ibm.csv")
      (.then
       (fn [response]
         (let [parse-date (.timeParse js/d3 "%d-%b-%y")
               data (->> response js->clj
                         (map (fn [d]
                                {:date (parse-date (get d "date")) :close (js/parseFloat (get d "close"))})
                              ) clj->js)
               svg (.. js/d3 (select (str "#" svg-el-id)))

               x-scale (-> (.scaleUtc js/d3)
                           (.domain (.extent js/d3 data (fn [d] (do
                                                                  (.log js/console d)
                                                                  (.-date d)))))
                           (.range (clj->js [0 300])))

               y-scale (-> (.scaleLinear js/d3)
                           (.domain (clj->js [0 1000]))
                           (.range (clj->js [0 300])))

               ]

           ;; (-> svg
           ;;     (.append "g")
           ;;     (.call x-axis))
           ;; (-> svg
           ;;     (.append "g")
           ;;     (.call y-axis))

           (-> svg
               (.append "path")
               (.datum data)
               (.attr "fill" "none")
               (.attr "stroke" "steelblue")
               (.attr "stroke-width" 1.5)
               (.attr "stroke-linejoin" "round")
               (.attr "stroke-linecap" "round")
               (.attr "d" (-> (.line js/d3)
                              (.defined (fn [d] (not (js/isNaN (.-value d)))))
                              (.x (fn [d] (x-scale (.-date d))))
                              (.y (fn [d] (y-scale (.-value d))))))

               )


           ;; response
           (.log js/console data)
           )
         ))))

(defn line-chart []
  (reagent/create-class
   {:display-name "line-chart"
    :reagent-render (fn [this] [:div#line-chart-container [:svg [:g.graph]]])
    :component-did-mount #(make-line-chart "line-chart-container")}))
