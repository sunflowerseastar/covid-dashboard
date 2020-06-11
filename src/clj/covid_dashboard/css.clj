(ns covid-dashboard.css
  (:require [garden.def :refer [defstyles]]))

(defstyles screen
  [:.home-panel {:height "100vh" :background "lavender"}]

  [:.home-col-center {:background "#e1e1e1"}]
  [:.home-col-left {:background "#bbb"}]
  [:.home-col-right {:background "#999"}]

  [:.canvas-container [:canvas {:width "100% !important" :height "100% !important"}]]

  [:.border {:border "3px solid green !important"}]
  )
