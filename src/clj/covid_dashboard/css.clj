(ns covid-dashboard.css
  (:require [garden.def :refer [defstyles]]))

(defstyles screen
  [:.home-panel {:height "100vh" :background "lavender"}]

  [:.home-col-center {:background "#e1e1e1"}]
  [:.home-col-left {:background "#bbb"}]
  [:.home-col-right {:background "#999"}]

  [:.d3-container {:border "1px solid black" :width "100% !important"}]
  [:#d3-bubble-map-container {:border "1px solid magenta"}]
  [:.canvas-container [:canvas {:width "100% !important" :height "100% !important"}]]

  [:.border {:border "3px solid green !important"}]

  [:#d3-line-chart-container
   [:.axis [:path {:fill "none" :stroke "#000" :shape-rendering "crispedges"}]
    [:line {:fill "none" :stroke "#000" :shape-rendering "crispedges"}]
    [:&:.x [:path {:displays "none"}]]]
   [:.line {:fill "none" :stroke "steelblue" :stroke-width "1.5px"}]]
  )
