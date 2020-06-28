(ns covid-dashboard.css
  (:require [garden.def :refer [defstyles]]))

(defstyles screen
  [:.home-page {:height "100vh" :padding "10px" :box-sizing "border-box"}]

  [:a {:cursor "pointer"}]
  [:td {:padding "3px 4px 2px 2px"}]

  [:.home-col-left :.home-col-right {:z-index 1}]

  [:.panel-3-1 {:position "absolute" :left 0 :top 0 :bottom 0 :width "100%"}]

  [:.panel {:border "1px solid #e1e1e1"}]
  [:.panel-interior {:background "#fdfdfdcc"}]

  [:.controls {:border "3px solid orange"}]

  [:.padding-1 {:box-sizing "border-box" :padding "8px 5px 8px 10px"}]
  [:.padding-2 {:box-sizing "border-box" :padding "8px"}]
  [:.z-index-1 {:z-index 1}]

  [:.margin-0-auto {:margin "0 auto"}]
  [:.children-align-self-center [:div {:align-self "center"}]]

  [:.graticule {:fill "none"
                :stroke "#eee"
                :stroke-width ".5px"
                :stroke-opacity 0.4}]

  [:.scroll-y-auto {:overflow-y "auto"}]
  [:.svg-container {
                    ;; :border "1px solid"
                    :position "relative" :top "50%" :transform "translateY(-50%)" :width "100%"
                    :overflow "visible"
                    }])
