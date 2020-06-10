(ns covid-dashboard.css
  (:require [garden.def :refer [defstyles]]))

(defstyles screen
  [:body {:color "orange"}]
  [:.middle {:background "#e1e1e1" :height "100vh"}]
  [:.level1 {:color "green"}])
