(ns covid-dashboard.css
  (:require [garden.def :refer [defstyles]]))

(defstyles screen
  [:.home-page {:height "100vh" :padding "10px" :box-sizing "border-box"}]

  [:td {:padding "3px 4px 2px 2px"}]

  ;; [:.home-col-left {:background "yellow"}]

  [:.panel {:border "1px solid #e1e1e1"}]
  [:.panel-interior {:background "#fdfdfd"}]

  [:.controls {:border "3px solid orange"}]

  [:.padding-1 {:box-sizing "border-box" :padding "8px 5px 8px 10px"}]

  [:.margin-0-auto {:margin "0 auto"}]
  [:.children-align-self-center [:div {:align-self "center"}]]

  [:.scroll-y-auto {:overflow-y "auto"}]
  [:.svg-container {:position "relative" :top "50%" :transform "translateY(-50%)" :width "100%"}])
