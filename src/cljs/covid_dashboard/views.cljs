(ns covid-dashboard.views
  (:require
   [re-frame.core :as re-frame]
   [re-com.core :as re-com]
   [breaking-point.core :as bp]
   [covid-dashboard.subs :as subs]
   [covid-dashboard.vegas :refer [line-plot-vega-lite map-us-chloropleth-vega]]
   [oz.core :as oz]
   ))


;; home

(defn home-title []
  (let [name (re-frame/subscribe [::subs/name])]
    [re-com/title
     :label (str "Hello from " @name ". This is the Home Page.")
     :level :level1]))

(defn link-to-about-page []
  [re-com/hyperlink-href
   :label "go to About Page"
   :href "#/about"])

(defn home-col-center []
  [:div.container
   [home-title]
   [link-to-about-page]
   [:div
    [:h3 (str "screen-width: " @(re-frame/subscribe [::bp/screen-width]))]
    [:h3 (str "screen: " @(re-frame/subscribe [::bp/screen]))]]])

(defn home-col-left []
  [re-com/v-box
   :gap "1em"
   :children [[re-com/box :size "1" :child [line-plot-vega-lite]]
              [re-com/box :size "1" :child "bottom"]]])

(defn home-col-right []
  [re-com/v-box
   :gap "1em"
   :children [[re-com/box :size "1" :child "right top"]
              [re-com/box :size "1" :child [map-us-chloropleth-vega]]]])

(defn home-panel []
  [re-com/h-box
   :class "middle"
   :gap "1em"
   :children [[re-com/box :size "1" :child [home-col-left]]
              [re-com/box :size "1" :child [home-col-center]]
              [re-com/box :size "1" :child [home-col-right]]]])


;; about

(defn about-title []
  [re-com/title
   :label "This is the About Page."
   :level :level1])

(defn link-to-home-page []
  [re-com/hyperlink-href
   :label "go to Home Page"
   :href "#/"])

(defn about-panel []
  [re-com/v-box
   :gap "1em"
   :children [[about-title]
              [link-to-home-page]]])


;; main

(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    :about-panel [about-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [re-com/v-box
     :height "100%"
     :children [[panels @active-panel]]]))
