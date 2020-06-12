(ns covid-dashboard.d3s
  (:require
   [breaking-point.core :as bp]
   [re-frame.core :as re-frame]
   [reagent.core :as r]
   [cljsjs.d3 :as d3]))

(def margin {:top 20, :right 20, :bottom 30, :left 50})

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

(defn get-svg-2 [margin width height]
  (.. js/d3 (select "#d3-bubble-map-container svg")
      (attr "width" (+ width (:left margin) (:right margin)))
      (attr "height" (+ height (:top margin) (:bottom margin)))
      (append "g")
      (attr "transform" (str "translate(" (:left margin) \, (:top margin) \) ))))

(defn bubble-map [starting-width]
  (println "bubble-map")
  (let [width (- starting-width (:left margin) (:right margin))
        height (* starting-width 0.6)]
    (-> (.json js/d3 "https://covid-dashboard.sunflowerseastar.com/data/population.json")
        (.then
         (fn [population-data]
           (-> (.json js/d3  "https://covid-dashboard.sunflowerseastar.com/data/counties-albers-10m.json")
               (.then
                (fn [us]
                  ;; (println us)
                  (let [data (js/Map.)
                        _ (dorun (map #(.set data % (get data %)) (js->clj population-data)))
                        svg (get-svg-2 margin width height)]

                    (do
                      ;; (println "before2")
                      ;; (dorun (map #(.set data % (get data %)) (js->clj population-data)))
                      ;; (println data)
                      (.. svg (append "path")
                          ;; (datum (.feature js/d3 us (-> us .-objects .-nation)))
                          )


                      )
                    ;; (.forIn population-data #(.set data % (get data %)))
                    )))
               ;; (set-domains x y data)
               ;; (build-x-axis height svg x-axis)
               ;; (build-y-axis svg y-axis)
               ;; (add-line svg line data)
               ))))
    ))

(defn bubble-map-d3 []
  (r/create-class
   {:display-name "bubble-map-d3"
    :reagent-render (fn [this] [:div#d3-bubble-map-container [:svg {:viewBox [0 0 975 610]}]])
    :component-did-mount #(bubble-map (/ @(re-frame/subscribe [::bp/screen-width]) 3))}))
