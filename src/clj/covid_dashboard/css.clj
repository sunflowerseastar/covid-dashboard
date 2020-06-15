(ns covid-dashboard.css
  (:require [garden.def :refer [defstyles]]))

(defstyles screen

  ;; home
  [:.home-panel {:height "100vh" :background "lavender"}]
  [:.home-col-center {:background "#e1e1e1" :border "1px solid"}]
  [:.home-col-left {:background "#bbb"}]
  [:.home-col-right {:background "#999"}]

  ;; d3
  [:.svg-container {:width "100%"}]

  ;; vegas
  [:.vegas-container [:canvas {:width "100% !important" :height "100% !important"}]]

  ;; utilities
  [:.border {:border "3px solid green !important"}]

  )
