(ns covid-dashboard.css
  (:require [garden.def :refer [defstyles]]))

(defstyles screen
  ;; [:.home-container {:border "1px solid magenta"}]
  [:.home-page {:border "1px solid cyan" :height "100vh"}]
  [:.svg-container {:position "relative" :top "50%" :transform "translateY(-50%)" :width "100%"}])
