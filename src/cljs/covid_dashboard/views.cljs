(ns covid-dashboard.views
  (:require
   [covid-dashboard.d3s :refer [bubble-map-covid-us-d3]]
   [re-com.core :as re-com]
   [re-frame.core :as re-frame]
   [reagent.core :refer [create-class]]
   [covid-dashboard.subs :as subs]
   [cljsjs.d3 :as d3]))

(defn total-confirmed-panel []
  (let [total-confirmed (re-frame/subscribe [::subs/total-confirmed])]
    (when @total-confirmed
      [:div.total-confirmed-panel [:p "Total Confirmed"] [:p @total-confirmed]])))

(defn panel-2 []
  (let [confirmed-by-region (re-frame/subscribe [::subs/confirmed-by-region])]
    (when @confirmed-by-region
      [:div.panel-2 [:p "Confirmed Cases by Country/Region/Sovereignty"]
       [:table [:tbody (map (fn [[region value]]
                              [:tr {:key (str region value)} [:td region] [:td value]])
                            @confirmed-by-region)]]])))

(defn home-col-left []
  [re-com/v-box
   :class "home-col-left"
   :children [[re-com/box :child [total-confirmed-panel]]
              [re-com/box :size "1" :child [panel-2]]]])

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
      [re-com/v-box
       :height "100%"
       :children [[re-com/h-box
                   :class "home-page"
                   :children [[re-com/box :size "1" :child [home-col-left]]
                              [re-com/box :size "3" :class "home-col-center" :child [bubble-map-covid-us-d3]]
                              #_[re-com/box :size "1" :child [home-col-right]]]]]])}))
