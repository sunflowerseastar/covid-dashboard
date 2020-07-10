(ns covid-dashboard.line-charts
  (:require [covid-dashboard.subs :as subs]
            [breaking-point.core :as bp]
            [covid-dashboard.utility :as utility]
            [re-com.core :refer [box gap h-box v-box]]
            [re-frame.core :as re-frame]
            [tupelo.core :refer [spyx]]
            [reagent.core :as reagent]))

(defn parse-date [x] ((.timeParse js/d3 "%m/%d/%Y") x))

(defn line-chart [svg-el-id height line-chart-data]
  (let [margin 30

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

    ;; clean-up (for re-renders)
    (-> svg (.selectAll "g")
        (.remove))
    (-> svg (.selectAll "path")
        (.remove))

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

(defn line-chart-d3
  "This takes the current ('live') screen width to force updates on viewport size change, and the data.
  It returns an svg, and calls the d3 function to work on the svg."
  [live-width-to-force-updates line-chart-data]
  (let [height 200 svg-el-id "line-chart-root-svg"]
    (reagent/create-class
     {:display-name "line-chart-d3"
      :reagent-render (fn [comp] [:svg {:id svg-el-id :class "svg-container" :viewBox [0 0 300 height]}])
      :component-did-update (fn [comp] (js/setTimeout #(line-chart svg-el-id height line-chart-data) 500))
      :component-did-mount (fn [comp] (line-chart svg-el-id height line-chart-data))})))

(defn line-chart-global-confirmed-linear []
  (let [time-series-confirmed-global (re-frame/subscribe [::subs/time-series-confirmed-global])]
    (when @time-series-confirmed-global
      [:div.panel-interior.padding-2 [line-chart-d3 @(re-frame/subscribe [::bp/screen-width]) @time-series-confirmed-global]])))

(defn line-chart-global-daily-cases []
  (let [time-series-confirmed-global (re-frame/subscribe [::subs/time-series-confirmed-global])]
    (when @time-series-confirmed-global
      (let [daily-cases-data (->> @time-series-confirmed-global
                                  (partition 2 1)
                                  (map (fn [[[_ yesterday] [date today]]] (vector date (- today yesterday)))))]
        [:div.panel-interior.padding-2 [line-chart-d3 @(re-frame/subscribe [::bp/screen-width]) daily-cases-data]]))))





(defn line-chart-log [svg-el-id height line-chart-data]
  (let [margin 30

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
                                      (.ticks (/ width 80))
                                      (.tickSizeOuter 0)))
                           (.call (fn [g] (-> (.select g ".domain") (.remove))))))

        y-axis (fn [g] (-> (.attr g "transform" (str "translate(" margin ",9)"))
                           (.call (-> (.axisLeft js/d3 y-scale)
                                      (.ticks 5)
                                      (.tickFormat format-value)))
                           (.call (fn [g] (-> (.select g ".domain") (.remove))))
                           (.call (fn [g] (-> (.select g ".tick:last-of-type text")
                                              (.clone)
                                              (.attr "x" 6)
                                              (.attr "text-anchor" "start")
                                              (.text "logarithmic"))))))]

    (-> svg (.attr "viewBox" (clj->js [0 0 (-> (.node svg) (.-clientWidth)) height])))

    ;; clean-up (for re-renders)
    (-> svg (.selectAll "g")
        (.remove))
    (-> svg (.selectAll "path")
        (.remove))

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

(defn line-chart-log-d3
  "This takes the current ('live') screen width to force updates on viewport size change, and the data.
  It returns an svg, and calls the d3 function to work on the svg."
  [live-width-to-force-updates line-chart-data]
  (let [height 200 svg-el-id "line-chart-log-root-svg"]
    (reagent/create-class
     {:display-name "line-chart-log-d3"
      :reagent-render (fn [this] [:svg {:id svg-el-id :class "svg-container" :viewBox [0 0 300 height]}])
      :component-did-update (fn [comp] (js/setTimeout #(line-chart-log svg-el-id height line-chart-data) 500))
      :component-did-mount #(line-chart-log svg-el-id height line-chart-data)})))

(defn line-chart-global-confirmed-log []
  (let [time-series-confirmed-global (re-frame/subscribe [::subs/time-series-confirmed-global])]
    (when @time-series-confirmed-global
      [:div.panel-interior.padding-2 [line-chart-log-d3 @(re-frame/subscribe [::bp/screen-width]) @time-series-confirmed-global]])))
