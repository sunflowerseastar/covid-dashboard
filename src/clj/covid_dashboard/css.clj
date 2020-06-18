(ns covid-dashboard.css
  (:require [garden.def :refer [defstyles]]))

(defstyles screen
  [:.home-page {:height "100vh"}]
  [:.svg-container {:position "relative" :top "50%" :transform "translateY(-50%)" :width "100%"}])
