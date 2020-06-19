(ns covid-dashboard.views
  (:require
   [cljsjs.d3 :as d3]
   [covid-dashboard.d3s :as d3s]
   [covid-dashboard.subs :as subs]
   [re-com.core :as re-com]
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]))

(defn panel-1 []
  (let [total-confirmed (re-frame/subscribe [::subs/total-confirmed])]
    (when @total-confirmed
      [:div [:p "Total Confirmed"] [:h3 @total-confirmed]])))

(defn panel-2 []
  (let [confirmed-by-region (re-frame/subscribe [::subs/confirmed-by-region])]
    (when @confirmed-by-region
      [:div.panel-2 [:p "Confirmed Cases by Country/Region/Sovereignty"]
       [:table [:tbody (map (fn [[region value]]
                              [:tr {:key (str region value)} [:td region] [:td value]])
                            @confirmed-by-region)]]])))

(defn panel-4 []
  (let [global-deaths (re-frame/subscribe [::subs/global-deaths])]
    (when-let [{:keys [deaths-by-region total-deaths]} @global-deaths]
      [:div.panel-4 [:p "Global Deaths"]
       [:h3 total-deaths]
       [:table [:tbody (map (fn [[region value]]
                              [:tr {:key (str region value)} [:td region] [:td value]])
                            deaths-by-region)]]])))

(defn home-col-left []
  [re-com/v-box
   :class "home-col-left"
   :children [[re-com/box :child [panel-1]]
              [re-com/box :size "1" :child [panel-2]]]])

(defn home-col-right []
  [re-com/v-box
   :class "home-col-right"
   :size "auto"
   :children [[re-com/box :size "1"
               :child [re-com/h-box :size "1"
                       :children [[re-com/box :size "1" :child [panel-4]]
                                  [re-com/box :size "1" :child "panel 5"]]]]
              [re-com/box :size "1" :child "panel 6"]]])

(defn home-page []
  (reagent/create-class
   {:display-name "home-page"
    :reagent-render
    (fn [this]
      [re-com/v-box
       :height "100%"
       :children [[re-com/h-box
                   :class "home-page"
                   :children [[re-com/box :size "1" :child [home-col-left]]
                              [re-com/box :size "3" :class "home-col-center" :child [d3s/bubble-map-covid-us-d3]]
                              [re-com/box :size "1" :child [home-col-right]]]]]])}))
