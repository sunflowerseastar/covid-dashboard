(ns covid-dashboard.css
  (:require [garden.def :refer [defstyles]]))

(defstyles screen
  [:.home-page {:height "100vh"}]

  [:.display-flex {:width "100%"}]

  [:.home-col-left {:background "yellow"}]

  [:.panel-2 {:background "#eee"}]
  [:.panel-2-scroll {:overflow-y "auto"}]

  [:.svg-container {:position "relative" :top "50%" :transform "translateY(-50%)" :width "100%"}])
