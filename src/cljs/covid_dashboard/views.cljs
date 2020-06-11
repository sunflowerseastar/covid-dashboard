(ns covid-dashboard.views
  (:require
   [breaking-point.core :as bp]
   [covid-dashboard.subs :as subs]
   [covid-dashboard.vegas :refer [line-plot-vega-lite map-us-chloropleth-vega]]
   [covid-dashboard.d3s :refer [line-chart-d3]]
   [oz.core :as oz]
   [re-com.core :as re-com]
   [re-frame.core :as re-frame]
   [cljsjs.d3 :as d3]))

;; home

(defn home-col-left []
  [re-com/v-box
   :class "home-col-left"
   :gap "1em"
   :children [[re-com/box :size "1" :child [line-plot-vega-lite]]
              [re-com/box :size "1" :child [line-chart-d3]]]])

(defn home-col-center []
  [:div.home-col-center
   [:div.canvas-container [map-us-chloropleth-vega]]])

(defn home-col-right []
  [re-com/v-box
   :class "home-col-right"
   :gap "1em"
   :size "auto"
   :children [[re-com/box :size "1" :child "right top"]
              [re-com/box :size "1" :child [:div.canvas-container [map-us-chloropleth-vega]]]]])

(defn home-panel []
  [re-com/h-box
   :class "home-panel"
   :gap "1em"
   :children [[re-com/box :size "1" :child [home-col-left]]
              [re-com/box :size "1" :child [home-col-center]]
              [re-com/box :size "1" :child [home-col-right]]]])

;; main

(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [re-com/v-box
     :height "100%"
     :children [[panels @active-panel]]]))
