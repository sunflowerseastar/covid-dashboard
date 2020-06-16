(ns covid-dashboard.views
  (:require
   [covid-dashboard.d3s :refer [bubble-map-covid-us-d3]]
   [re-com.core :as re-com]
   [re-frame.core :as re-frame]
   [reagent.core :refer [create-class]]
   [cljsjs.d3 :as d3]))

(defn home-page []
  (create-class
   {:display-name "home-page"
    :reagent-render
    (fn [this]
      [:div.home-container
       [:div.home-page [bubble-map-covid-us-d3]]])}))
