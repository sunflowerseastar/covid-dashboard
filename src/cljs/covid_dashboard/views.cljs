(ns covid-dashboard.views
  (:require
   [cljsjs.d3 :as d3]
   [oz.core :as oz]
   [covid-dashboard.components :refer [sub-panel-container]]
   [covid-dashboard.d3s :as d3s]
   [covid-dashboard.static :refer [gap-size]]
   [covid-dashboard.subs :as subs]
   [covid-dashboard.utility :as utility]
   [re-com.core :refer [box gap h-box v-box]]
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]))

(defn panel-1 []
  (let [total-confirmed (re-frame/subscribe [::subs/total-confirmed])]
    (when @total-confirmed
      [:div.padding-1 [:h4 "Total Confirmed"] [:h3 (utility/nf @total-confirmed)]])))

(defn panel-2-0 []
  (let [confirmed-by-country (re-frame/subscribe [::subs/confirmed-by-country])]
    (when @confirmed-by-country
      [v-box :size "1" :children
       [[box :class "padding-1" :child [:h4 "Confirmed Cases by Country/Region"]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[country value]]
                                [:tr {:key (str country value)}
                                 [:td.bold (utility/nf value)] [:td country]])
                              @confirmed-by-country)]]]]])))

(defn panel-2-1 []
  (let [confirmed-by-province (re-frame/subscribe [::subs/confirmed-by-province])]
    (when @confirmed-by-province
      [v-box :size "1" :children
       [[box :class "padding-1" :child [:h4 "Confirmed Cases by State/Province"]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[value province country]]
                                [:tr {:key (str province value)} [:td.bold (utility/nf value)] [:td (str province ", " country)]])
                              @confirmed-by-province)]]]]])))

(defn panel-2-2 []
  (let [confirmed-by-us-county (re-frame/subscribe [::subs/confirmed-by-us-county])]
    (when @confirmed-by-us-county
      [v-box :size "1" :children
       [[box :class "padding-1" :child [:h4 "Confirmed Cases by U.S. County"]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[value us-county country]]
                                [:tr {:key (str us-county value)} [:td.bold (utility/nf value)] [:td (str us-county ", " country)]])
                              @confirmed-by-us-county)]]]]])))

(defn panel-4-0 []
  (let [global-deaths (re-frame/subscribe [::subs/global-deaths])]
    (when-let [{:keys [deaths-by-country total-deaths]} @global-deaths]
      [v-box :size "1" :children
       [[box :class "padding-1" :child [:div [:h4 "Global Deaths"] [:h3 (utility/nf total-deaths)]]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[country value]]
                                [:tr {:key (str country value)} [:td.bold (utility/nf value)] [:td country]])
                              deaths-by-country)]]]]])))

(defn panel-4-1 []
  (let [global-recovered (re-frame/subscribe [::subs/global-recovered])]
    (when-let [{:keys [recovered-by-country total-recovered]} @global-recovered]
      [v-box :size "1" :children
       [[box :class "padding-1" :child [:div [:h4 "Global Recovered"] [:h3 (utility/nf total-recovered)]]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[country value]]
                                [:tr {:key (str country value)} [:td.bold (utility/nf value)] [:td country]])
                              recovered-by-country)]]]]])))

(defn panel-5-1 []
  (let [us-states-deaths-recovered (re-frame/subscribe [::subs/us-states-deaths-recovered])]
    (when @us-states-deaths-recovered
      [v-box :size "1" :children
       [[box :class "padding-1" :child [:div [:h4 "US State Level"] [:h3 "Deaths, Recovered"]]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[state deaths recovered]]
                                [:tr {:key state} [:td.bold (utility/nf deaths)]
                                 [:td {:class (if recovered "bold" "light")} (if recovered (utility/nf recovered) "n/a")] [:td state]])
                              @us-states-deaths-recovered)]]]]])))

(defn panel-5-2 []
  (let [us-states-tested (re-frame/subscribe [::subs/us-states-tested])]
    (when-let [{:keys [tested-by-state total-tested]} @us-states-tested]
      [v-box :size "1" :children
       [[box :class "padding-1" :child [:div [:h4 "US People Tested"] [:h3 (utility/nf total-tested)]]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[state tested]]
                                [:tr {:key state} [:td.bold (utility/nf tested)] [:td state]])
                              tested-by-state)]]]]])))

(defn panel-5-3 []
  (let [us-states-hospitalized (re-frame/subscribe [::subs/us-states-hospitalized])]
    (when @us-states-hospitalized
      [v-box :size "1" :children
       [[box :class "padding-1" :child [:div [:h4 "US State Level"] [:h3 "Hospitalizations"]]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[state hospitalized]]
                                [:tr {:key state} [:td.bold (utility/nf hospitalized)] [:td state]])
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

(def curr-map (reagent/atom 0))

(defn panel-3-1 []
  (let [confirmed-by-country (re-frame/subscribe [::subs/confirmed-by-country])]
    (when @confirmed-by-country
      [:div.panel-3-1 [d3s/world-bubble-map-d3 @confirmed-by-country]])))

(defn panel-3-2 []
  [:div.panel-3-1 [d3s/bubble-map-covid-us-d3]])

(defn map-switcher [sub-panels]
  (reagent/with-let [sub-panel-count (count sub-panels)
                     sps ["Cumulative Confirmed Cases" "US - Confirmed by Population"]]
    [v-box :size "1" :children
     [[box :size "1" :child ""]
      [box :size "36px" :child
       [h-box :size "1" :class "children-align-self-center z-index-1 panel" :children
        [[box :child [:a.button {:on-click #(reset! curr-map (if (= (dec @curr-map) -1) (dec sub-panel-count) (dec @curr-map)))} "←"]]
         [box :size "1" :child [:p.margin-0-auto (str (get sps @curr-map) " " (inc @curr-map) "/" sub-panel-count)]]
         [box :child [:a.button {:on-click #(reset! curr-map (if (= (inc @curr-map) sub-panel-count) 0 (inc @curr-map)))} "→"]]]]]]]))

(defn loader []
  (let [is-fetching (re-frame/subscribe [::subs/is-fetching])]
    [:div.loader.fade {:class (when @is-fetching "is-active")}
     [:div.virion-container
      [:div.virion-container-inner
       [:img.virion {:src "images/virion-sat-fade_500.jpg"}]]]]))

(defn home-page []
  (let [maps [panel-3-1 panel-3-2]
        is-loaded (re-frame/subscribe [::subs/is-loaded])]
    (reagent/create-class
     {:display-name "home-page"
      :reagent-render
      (fn [this]
        [:<>
         [loader]
         [v-box
          :height "100%"
          :class (str "fade " (when @is-loaded "is-active"))
          :children [[(get maps @curr-map)]
                     [h-box :class "home-page" :gap gap-size :children
                      [[box :size "2" :child [home-col-left]]
                       [box :size "5" :class "home-col-center" :child [map-switcher maps]]
                       [box :size "3" :child [home-col-right]]]]]]])})))
