(ns covid-dashboard.css
  (:require [garden.def :refer [defstyles]]))

(defstyles screen
  [:body {:color "orange"}]
  [:.middle {:background "#e1e1e1" :height "100vh"}]
  [:.container [:canvas {:border "3px solid red !important"
                         ;; :width "100% !important"
                         ;; :height "100% !important"
                         }]]
  [:.level1 {:color "green"}])
