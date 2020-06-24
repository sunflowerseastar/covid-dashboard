(ns covid-dashboard.views
  (:require
   [cljsjs.d3 :as d3]
   [oz.core :as oz]
   [covid-dashboard.d3s :as d3s]
   [covid-dashboard.subs :as subs]
   [re-com.core :as re-com]
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]))

(def gap-size "10px")

(defn panel-1 []
  (let [total-confirmed (re-frame/subscribe [::subs/total-confirmed])]
    (when @total-confirmed
      [:div.panel-interior.padding-1 [:p "Total Confirmed"] [:h2 @total-confirmed]])))

(defn panel-2-0 []
  (let [confirmed-by-region (re-frame/subscribe [::subs/confirmed-by-region])]
    (when @confirmed-by-region
      [re-com/v-box :size "1" :class "panel-interior" :children
       [[re-com/box :class "padding-1" :child [:p "Confirmed Cases by Region"]]
        [re-com/box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[region value]]
                                [:tr {:key (str region value)} [:td region] [:td value]])
                              @confirmed-by-region)]]]]])))

(defn panel-2-1 []
  [:div [:p "panel-2-1"]])

(defn panel-2-2 []
  [:div [:p "panel-2-2"]])

(def curr (reagent/atom 0))

(defn panel-2 []
  (let [sub-panels [panel-2-0 panel-2-1 panel-2-2]
        sub-panel-count (count sub-panels)]
    [re-com/v-box :size "1" :children
     [[re-com/box :size "1" :child [(get sub-panels @curr)]]
      [re-com/box :size "50px" :child
       [re-com/h-box :size "1" :class "children-align-self-center" :children
        [[re-com/box :child [:a {:on-click #(reset! curr (if (= (dec @curr) -1) (dec sub-panel-count) (dec @curr)))} "left"]]
         [re-com/box :size "1" :child [:p.margin-0-auto (str "curr " @curr)]]
         [re-com/box :child [:a {:on-click #(reset! curr (if (= (inc @curr) sub-panel-count) 0 (inc @curr)))} "right"]]]]]]]))

(defn panel-4 []
  (let [global-deaths (re-frame/subscribe [::subs/global-deaths])]
    (when-let [{:keys [deaths-by-region total-deaths]} @global-deaths]
      [re-com/v-box :size "1" :class "panel-interior" :children
       [[re-com/box :class "padding-1" :child [:div [:p "Global Deaths"] [:h2 total-deaths]]]
        [re-com/box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[region value]]
                                [:tr {:key (str region value)} [:td region] [:td value]])
                              deaths-by-region)]]]]])))

(defn panel-5 []
  (let [us-state-level-deaths-recovered (re-frame/subscribe [::subs/us-state-level-deaths-recovered])]
    (when @us-state-level-deaths-recovered
      [re-com/v-box :size "1" :class "panel-interior" :children
       [[re-com/box :class "padding-1" :child [:div [:p "US State Level"] [:h3 "Deaths, Recovered"]]]
        [re-com/box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[state deaths recovered]]
                                [:tr {:key state} [:td state] [:td deaths] [:td recovered]])
                              @us-state-level-deaths-recovered)]]]]])))

(defn panel-6 []
  (let [time-series-confirmed-global (re-frame/subscribe [::subs/time-series-confirmed-global])]
    (when @time-series-confirmed-global
      [:div.panel-interior.padding-1 [d3s/line-chart-d3 @time-series-confirmed-global]])))

(defn home-col-left []
  [re-com/v-box
   :class "home-col-left"
   :gap gap-size
   :size "auto"
   :children [[re-com/box :class "panel" :child [panel-1]]
              [re-com/box :size "1" :class "panel" :child [panel-2]]]])

(defn home-col-right []
  [re-com/v-box
   :class "home-col-right"
   :gap gap-size
   :size "auto"
   :children [[re-com/box :size "1" :child
               [re-com/h-box :size "1" :gap gap-size :children
                [[re-com/box :size "1" :class "panel" :child [panel-4]]
                 [re-com/box :size "1" :class "panel" :child [panel-5]]]]]
              [re-com/box :class "panel" :child [panel-6]]]])

(defn home-page []
  (reagent/create-class
   {:display-name "home-page"
    :reagent-render
    (fn [this]
      [re-com/v-box
       :height "100%"
       :children [[re-com/h-box :class "home-page" :gap gap-size :children
                   [[re-com/box :size "2" :child [home-col-left]]
                    [re-com/box :size "5" :class "home-col-center" :child [d3s/bubble-map-covid-us-d3]]
                    [re-com/box :size "3" :child [home-col-right]]]]]])}))
