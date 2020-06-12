(ns covid-dashboard.d3s
  (:require
   [breaking-point.core :as bp]
   [re-frame.core :as re-frame]
   [reagent.core :as r]
   [cljsjs.d3 :as d3]))

(defn get-scales [width height]
  [(.. js/d3 -time scale (range #js [0 width]))
   (.. js/d3 -scale linear (range #js [height 0]))])

(defn get-axes [x y]
  [(.. js/d3 -svg axis (scale x) (orient "bottom"))
   (.. js/d3 -svg axis (scale y) (orient "left"))])

(defn get-line [x y]
  (.. js/d3 -svg line
      (x #(x (.-date %)))
      (y #(y (.-close %)))))

(defn get-svg [margin width height]
  (.. js/d3 (select "#d3-container svg")
      (attr "width" (+ width (:left margin) (:right margin)))
      (attr "height" (+ height (:top margin) (:bottom margin)))
      (append "g")
      (attr "transform" (str "translate(" (:left margin) \, (:top margin) \) ))))

(defn coerce-datum [parse-date d]
  (aset d "date" (parse-date (.-date d)))
  (aset d "close" (js/parseFloat (.-close d))))

(defn set-domains [x y data]
  (.domain x (.extent js/d3 data #(.-date %)))
  (.domain y (.extent js/d3 data #(.-close %))))

(defn build-x-axis [height svg x-axis]
  (.. svg (append "g")
      (attr "class" "x axis")
      (attr "transform" (str "translate(0," height \)))
      (call x-axis)))

(defn build-y-axis [svg y-axis]
  (.. svg (append "g")
      (attr "class" "y axis")
      (call y-axis)
      (append "text")
      (attr "transform" "rotate(-90)")
      (attr "y" 6)
      (attr "dy" ".71em")
      (style "text-anchor" "end")
      (text "Price ($)")))

(defn add-line [svg line data]
  (.. svg (append "path")
      (datum data)
      (attr "class" "line")
      (attr "d" line)))

(defn ibm-stock [starting-width]
  (do (println "ibm-stock 234234!")
      (let [margin {:top 20, :right 20, :bottom 30, :left 50}
            width (- starting-width (:left margin) (:right margin))
            height (* starting-width 0.6)
            parse-date (.. js/d3 -time (format "%d-%b-%y") -parse)
            [x y] (get-scales width height)
            [x-axis y-axis] (get-axes x y)
            line (get-line x y)
            svg (get-svg margin width height)]
        (do
          (println "HISDFSDF")
          (println svg)
          (.csv js/d3 "https://covid-dashboard.sunflowerseastar.com/data/ibm.csv"
              (fn [error data]
                (do (println data)
                    (.forEach data
                              #(coerce-datum parse-date %)))

                (set-domains x y data)

                (build-x-axis height svg x-axis)
                (build-y-axis svg y-axis)
                (add-line svg line data)))))))

(defn svg-markers []
  [:defs
   [:marker {:id "end-arrow" :viewBox "0 -5 10 10" :refX 17 :refY 0
             :markerWidth 6 :markerHeight 6 :markerUnits "strokeWidth"
             :orient "auto"}
    [:path {:d "M0,-5L10,0L0,5"}]]])

(defn graph-render-2 []
  (println "graph-render-2")
    [:div#d3-container [:svg [:g.graph]]])

(defn line-chart-d3 []
  (r/create-class
   {:display-name "line-chart-d3"
    :reagent-render graph-render-2
    :component-did-mount #(ibm-stock (/ @(re-frame/subscribe [::bp/screen-width]) 3))}))
