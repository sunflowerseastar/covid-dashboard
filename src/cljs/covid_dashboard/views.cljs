(ns covid-dashboard.views
  (:require
   [cljsjs.d3 :as d3]
   [oz.core :as oz]
   [covid-dashboard.components :refer [sub-panel-container]]
   [covid-dashboard.d3s :as d3s]
   [covid-dashboard.static :refer [gap-size]]
   [covid-dashboard.subs :as subs]
   [re-com.core :refer [box h-box v-box]]
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]))

(defn panel-1 []
  (let [total-confirmed (re-frame/subscribe [::subs/total-confirmed])]
    (when @total-confirmed
      [:div.panel-interior.padding-1 [:p "Total Confirmed"] [:h2 @total-confirmed]])))

(defn panel-2-0 []
  (let [confirmed-by-region (re-frame/subscribe [::subs/confirmed-by-region])]
    (when @confirmed-by-region
      [v-box :size "1" :class "panel-interior" :children
       [[box :class "padding-1" :child [:p "Confirmed Cases by Country/Region"]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[region value]]
                                [:tr {:key (str region value)} [:td value] [:td region]])
                              @confirmed-by-region)]]]]])))

(defn panel-2-1 []
  (let [confirmed-by-province (re-frame/subscribe [::subs/confirmed-by-province])]
    (when @confirmed-by-province
      [v-box :size "1" :class "panel-interior" :children
       [[box :class "padding-1" :child [:p "Confirmed Cases by State/Province"]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[value province country]]
                                [:tr {:key (str province value)} [:td value] [:td province] [:td country]])
                              @confirmed-by-province)]]]]])))

(defn panel-2-2 []
  (let [confirmed-by-us-county (re-frame/subscribe [::subs/confirmed-by-us-county])]
    (when @confirmed-by-us-county
      [v-box :size "1" :class "panel-interior" :children
       [[box :class "padding-1" :child [:p "Confirmed Cases by U.S. County"]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[value us-county country]]
                                [:tr {:key (str us-county value)} [:td value] [:td us-county] [:td country]])
                              @confirmed-by-us-county)]]]]])))

(defn panel-4-0 []
  (let [global-deaths (re-frame/subscribe [::subs/global-deaths])]
    (when-let [{:keys [deaths-by-region total-deaths]} @global-deaths]
      [v-box :size "1" :class "panel-interior" :children
       [[box :class "padding-1" :child [:div [:p "Global Deaths"] [:h2 total-deaths]]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[region value]]
                                [:tr {:key (str region value)} [:td region] [:td value]])
                              deaths-by-region)]]]]])))

(defn panel-4-1 []
  (let [global-recovered (re-frame/subscribe [::subs/global-recovered])]
    (when-let [{:keys [recovered-by-region total-recovered]} @global-recovered]
      [v-box :size "1" :class "panel-interior" :children
       [[box :class "padding-1" :child [:div [:p "Global Recovered"] [:h2 total-recovered]]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[region value]]
                                [:tr {:key (str region value)} [:td region] [:td value]])
                              recovered-by-region)]]]]])))

(defn panel-5-1 []
  (let [us-states-deaths-recovered (re-frame/subscribe [::subs/us-states-deaths-recovered])]
    (when @us-states-deaths-recovered
      [v-box :size "1" :class "panel-interior" :children
       [[box :class "padding-1" :child [:div [:p "US State Level"] [:h3 "Deaths, Recovered"]]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[state deaths recovered]]
                                [:tr {:key state} [:td state] [:td deaths] [:td recovered]])
                              @us-states-deaths-recovered)]]]]])))

(defn panel-5-2 []
  (let [us-states-tested (re-frame/subscribe [::subs/us-states-tested])]
    (when-let [{:keys [tested-by-state total-tested]} @us-states-tested]
      [v-box :size "1" :class "panel-interior" :children
       [[box :class "padding-1" :child [:div [:p "US People Tested"] [:h2 total-tested]]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[state deaths recovered]]
                                [:tr {:key state} [:td state] [:td deaths] [:td recovered]])
                              tested-by-state)]]]]])))

(defn panel-5-3 []
  (let [us-states-hospitalized (re-frame/subscribe [::subs/us-states-hospitalized])]
    (when @us-states-hospitalized
      [v-box :size "1" :class "panel-interior" :children
       [[box :class "padding-1" :child [:div [:p "US State Level"] [:h3 "Hospitalizations"]]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[state deaths recovered]]
                                [:tr {:key state} [:td state] [:td deaths] [:td recovered]])
                              @us-states-hospitalized)]]]]])))

(defn panel-6-0 []
  (let [time-series-confirmed-global (re-frame/subscribe [::subs/time-series-confirmed-global])]
    (when @time-series-confirmed-global
      [:div.panel-interior.padding-2 [d3s/line-chart-d3 @time-series-confirmed-global]])))

(defn panel-6-1 []
  (let [time-series-confirmed-global (re-frame/subscribe [::subs/time-series-confirmed-global])]
    (when @time-series-confirmed-global
      [:div.panel-interior.padding-2 [d3s/line-chart-log-d3 @time-series-confirmed-global]])))

(defn panel-6-2 []
  (let [time-series-confirmed-global (re-frame/subscribe [::subs/time-series-confirmed-global])]
    (when @time-series-confirmed-global
      (let [daily-cases-data (->> @time-series-confirmed-global
                                  (partition 2 1)
                                  (map (fn [[[_ yesterday] [date today]]] (vector date (- today yesterday)))))]
        [:div.panel-interior.padding-2 [d3s/line-chart-d3 daily-cases-data]]))))

(defn home-col-left []
  [v-box
   :class "home-col-left"
   :gap gap-size
   :size "auto"
   :children [[box :class "panel" :child [panel-1]]
              [box :size "1" :class "panel" :child
               [sub-panel-container [["Confirmed Country" panel-2-0]
                                     ["Confirmed State" panel-2-1]
                                     ["Confirmed County" panel-2-2]]]]]])

(defn home-col-right []
  [v-box
   :class "home-col-right"
   :gap gap-size
   :size "auto"
   :children [[box :size "1" :child
               [h-box :size "1" :gap gap-size :children
                [[box :size "1" :class "panel" :child [sub-panel-container [["Global Deaths" panel-4-0]
                                                                            ["Global Recovered" panel-4-1]]]]
                 [box :size "1" :class "panel" :child [sub-panel-container [["US Deaths/Recovered" panel-5-1]
                                                                            ["US Tested" panel-5-2]
                                                                            ["US Hospitalized" panel-5-3]]]]]]]
              [box :class "panel" :size "255px" :child [sub-panel-container [["Global Confirmed" panel-6-0]
                                                                             ["Global Confirmed" panel-6-1]
                                                                             ["Global Daily Cases" panel-6-2]]]]]])

(defn home-page []
  (reagent/create-class
   {:display-name "home-page"
    :reagent-render
    (fn [this]
      [v-box
       :height "100%"
       :children [[h-box :class "home-page" :gap gap-size :children
                   [[box :size "2" :child [home-col-left]]
                    [box :size "5" :class "home-col-center" :child [d3s/bubble-map-covid-us-d3]]
                    [box :size "3" :child [home-col-right]]]]]])}))
