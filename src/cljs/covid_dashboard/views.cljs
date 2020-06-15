(ns covid-dashboard.views
  (:require
   [breaking-point.core :as bp]
   [covid-dashboard.subs :as subs]
   [covid-dashboard.d3s :refer [bubble-map-covid-us-d3]]
   [re-com.core :as re-com]
   [re-frame.core :as re-frame]
   [reagent.core :refer [create-class]]
   [cljsjs.d3 :as d3]))

;; home

(defn home-col-left []
  [re-com/v-box
   :class "home-col-left"
   :children [[re-com/box :size "1" :child "x"]
              [re-com/box :size "1" :child "x"]]])

(defn home-col-center []
  [:div.home-col-center [bubble-map-covid-us-d3]])

(defn home-col-right []
  [re-com/v-box
   :class "home-col-right"
   :size "auto"
   :children [[re-com/box :size "1" :child "x"]
              [re-com/box :size "1" :child "x"]]])

(defn home-page []
  (create-class
   {:display-name "home-page"
    :reagent-render
    (fn [this]
      [re-com/h-box
       :class "home-page"
       :children [[re-com/box :size "1" :child [home-col-left]]
                  [re-com/box :size "3" :class "home-col-center" :child [bubble-map-covid-us-d3]]
                  [re-com/box :size "1" :child [home-col-right]]]])}))

;; main

(defn- pages [page-name]
  (case page-name
    :home-page [home-page]))

(defn show-page [page-name]
  [pages page-name])

(defn main-page []
  (let [active-page (re-frame/subscribe [::subs/active-page])]
    [re-com/v-box
     :height "100%"
     :children [[pages @active-page]]]))
