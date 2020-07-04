(ns covid-dashboard.views
  (:require [covid-dashboard.components :refer [sub-panel-container]]
            [breaking-point.core :as bp]
            [covid-dashboard.d3s :as d3s]
            [covid-dashboard.static :refer [control-bar-height-desktop gap-size duration-2]]
            [covid-dashboard.subs :as subs]
            [covid-dashboard.utility :as utility]
            [re-com.core :refer [box gap h-box v-box]]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]))

(defn table-totals []
  (let [total-confirmed (re-frame/subscribe [::subs/total-confirmed])]
    (when @total-confirmed
      [:div.padding-1 [:h4 "Total Confirmed"] [:h3 (utility/nf @total-confirmed)]])))

(defn table-confirmed-country []
  (let [confirmed-by-country (re-frame/subscribe [::subs/confirmed-by-country])]
    (when @confirmed-by-country
      [v-box :size "1" :children
       [[box :class "padding-1" :child [:h4 "Confirmed Cases by Country/Region"]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[country value]]
                                [:tr {:key (str country value)}
                                 [:td.bold (utility/nf value)] [:td country]])
                              @confirmed-by-country)]]]]])))

(defn table-confirmed-state []
  (let [confirmed-by-province (re-frame/subscribe [::subs/confirmed-by-province])]
    (when @confirmed-by-province
      [v-box :size "1" :children
       [[box :class "padding-1" :child [:h4 "Confirmed Cases by State/Province"]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[value province country]]
                                [:tr {:key (str province value)} [:td.bold (utility/nf value)] [:td (str province ", " country)]])
                              @confirmed-by-province)]]]]])))

(defn table-confirmed-county []
  (let [confirmed-by-us-county (re-frame/subscribe [::subs/confirmed-by-us-county])]
    (when @confirmed-by-us-county
      [v-box :size "1" :children
       [[box :class "padding-1" :child [:h4 "Confirmed Cases by U.S. County"]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[value us-county country]]
                                [:tr {:key (str us-county value)} [:td.bold (utility/nf value)] [:td (str us-county ", " country)]])
                              @confirmed-by-us-county)]]]]])))

(defn table-global-deaths []
  (let [global-deaths (re-frame/subscribe [::subs/global-deaths])]
    (when-let [{:keys [deaths-by-country total-deaths]} @global-deaths]
      [v-box :size "1" :children
       [[box :class "padding-1" :child [:div [:h4 "Global Deaths"] [:h3 (utility/nf total-deaths)]]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[country value]]
                                [:tr {:key (str country value)} [:td.bold (utility/nf value)] [:td country]])
                              deaths-by-country)]]]]])))

(defn table-global-recovered []
  (let [global-recovered (re-frame/subscribe [::subs/global-recovered])]
    (when-let [{:keys [recovered-by-country total-recovered]} @global-recovered]
      [v-box :size "1" :children
       [[box :class "padding-1" :child [:div [:h4 "Global Recovered"] [:h3 (utility/nf total-recovered)]]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[country value]]
                                [:tr {:key (str country value)} [:td.bold (utility/nf value)] [:td country]])
                              recovered-by-country)]]]]])))

(defn table-us-deaths-recovered []
  (let [us-states-deaths-recovered (re-frame/subscribe [::subs/us-states-deaths-recovered])]
    (when @us-states-deaths-recovered
      [v-box :size "1" :children
       [[box :class "padding-1" :child [:div [:h4 "US State Level"] [:h3 "Deaths, Recovered"]]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[state deaths recovered]]
                                [:tr {:key state} [:td.bold (utility/nf deaths)]
                                 [:td {:class (if recovered "bold" "light")} (if recovered (utility/nf recovered) "n/a")] [:td state]])
                              @us-states-deaths-recovered)]]]]])))

(defn table-us-tested []
  (let [us-states-tested (re-frame/subscribe [::subs/us-states-tested])]
    (when-let [{:keys [tested-by-state total-tested]} @us-states-tested]
      [v-box :size "1" :children
       [[box :class "padding-1" :child [:div [:h4 "US People Tested"] [:h3 (utility/nf total-tested)]]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[state tested]]
                                [:tr {:key state} [:td.bold (utility/nf tested)] [:td state]])
                              tested-by-state)]]]]])))

(defn table-us-hospitalized []
  (let [us-states-hospitalized (re-frame/subscribe [::subs/us-states-hospitalized])]
    (when @us-states-hospitalized
      [v-box :size "1" :children
       [[box :class "padding-1" :child [:div [:h4 "US State Level"] [:h3 "Hospitalizations"]]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[state hospitalized]]
                                [:tr {:key state} [:td.bold (utility/nf hospitalized)] [:td state]])
                              @us-states-hospitalized)]]]]])))

(defn line-chart-global-confirmed-linear []
  (let [time-series-confirmed-global (re-frame/subscribe [::subs/time-series-confirmed-global])]
    (when @time-series-confirmed-global
      [:div.panel-interior.padding-2 [d3s/line-chart-d3 @time-series-confirmed-global]])))

(defn line-chart-global-confirmed-log []
  (let [time-series-confirmed-global (re-frame/subscribe [::subs/time-series-confirmed-global])]
    (when @time-series-confirmed-global
      [:div.panel-interior.padding-2 [d3s/line-chart-log-d3 @time-series-confirmed-global]])))

(defn line-chart-global-daily-cases []
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
   :children [[box :class "panel" :child [table-totals]]
              [box :size "1" :class "panel" :child
               [sub-panel-container [["Confirmed Country" table-confirmed-country]
                                     ["Confirmed State" table-confirmed-state]
                                     ["Confirmed County" table-confirmed-county]]]]]])

(defn home-col-right []
  [v-box
   :class "home-col-right"
   :gap gap-size
   :size "auto"
   :children [;; panels 4 & 5...
              (if (= @(re-frame/subscribe [::bp/screen]) :tablet)
                ;; ...tablet - above and below
                [:<>
                 [box :size "1" :class "panel" :child
                  [sub-panel-container [["Global Deaths" table-global-deaths]
                                        ["Global Recovered" table-global-recovered]]]]
                 [box :size "1" :class "panel" :child
                  [sub-panel-container [["US Deaths/Recovered" table-us-deaths-recovered]
                                        ["US Tested" table-us-tested]
                                        ["US Hospitalized" table-us-hospitalized]]]]]
                ;; ...desktop - side by side
                [box :size "1" :child
                 [h-box :size "1" :gap gap-size :children
                  [[box :size "4" :class "panel" :child
                    [sub-panel-container [["Global Deaths" table-global-deaths]
                                          ["Global Recovered" table-global-recovered]]]]
                   [box :size "5" :class "panel" :child
                    [sub-panel-container [["US Deaths/Recovered" table-us-deaths-recovered]
                                          ["US Tested" table-us-tested]
                                          ["US Hospitalized" table-us-hospitalized]]]]]]])
              ;; panel 6, same either way
              [box :class "panel svg-pointer-events-none" :size "255px" :child
               [sub-panel-container [["Global Confirmed" line-chart-global-confirmed-linear]
                                     ["Global Confirmed" line-chart-global-confirmed-log]
                                     ["Global Daily Cases" line-chart-global-daily-cases]]]]]])

(def curr-map-old (reagent/atom 0))

(defn map-world-confirmed-by-country []
  (let [confirmed-by-country (re-frame/subscribe [::subs/confirmed-by-country])]
    (when @confirmed-by-country
      [:div.u-absolute-all [d3s/world-bubble-map-d3 @confirmed-by-country]])))

(defn map-us-confirmed-by-county []
  (let [confirmed-by-us-county-fips (re-frame/subscribe [::subs/confirmed-by-us-county-fips])]
    (when @confirmed-by-us-county-fips
      [:div.u-absolute-all [d3s/bubble-map-covid-us-d3 @confirmed-by-us-county-fips]])))

(defn map-switcher [sub-panels]
  (reagent/with-let [sub-panel-count (count sub-panels)
                     curr-map (re-frame/subscribe [::subs/curr-map])
                     is-transitioning (re-frame/subscribe [::subs/is-transitioning])
                     sps ["Cumulative Confirmed Cases" "US - Confirmed by Population"]
                     update-map #(do (re-frame/dispatch [:assoc-is-transitioning true])
                                     (js/setTimeout (fn [] (re-frame/dispatch [:update-curr-map %])) duration-2)
                                     (js/setTimeout (fn [] (re-frame/dispatch [:assoc-is-transitioning false])) (* duration-2 1.5)))]
    [v-box :size "1" :children
     [[box :size "1" :child ""]
      [box :size control-bar-height-desktop :child
       [h-box :size "1" :class "children-align-self-center z-index-1 panel" :children
        [[box :child [:a.button {:on-click #(when (not @is-transitioning) (update-map dec))} "←"]]
         [box :size "1" :child [:p.margin-0-auto (str (get sps (mod @curr-map sub-panel-count)) " " (inc @curr-map) "/" sub-panel-count)]]
         [box :child [:a.button {:on-click #(when (not @is-transitioning) (update-map inc))} "→"]]]]]]]))

(defn loader []
  (let [is-fetching (re-frame/subscribe [::subs/is-fetching])]
    [:div.loader.fade-duration-3 {:class (when @is-fetching "is-active")}
     [:div.virion-container
      [:div.virion-container-inner
       [:img.virion {:src "images/virion-sat-fade_500.jpg"}]]]]))

(defn home-page []
  (let [curr-map (re-frame/subscribe [::subs/curr-map])
        is-loaded (re-frame/subscribe [::subs/is-loaded])
        is-transitioning (re-frame/subscribe [::subs/is-transitioning])
        map-sub-panels [map-world-confirmed-by-country map-us-confirmed-by-county]
        screen (re-frame/subscribe [::bp/screen])]
    (reagent/create-class
     {:display-name "home-page"
      :reagent-render
      (fn [this]
        [:<>
         [loader]
         (if (= @screen :mobile)
           [v-box
            :height "100%"
            :class (str "fade-duration-3 " (when @is-loaded "is-active"))
            :children [[box :size "1" :class "panel" :child
                        [sub-panel-container
                         [["Total Confirmed" table-totals]
                          ["Confirmed Country" table-confirmed-country]
                          ["Confirmed State" table-confirmed-state]
                          ["Confirmed County" table-confirmed-county]
                          ["Confirmed Country" map-world-confirmed-by-country]
                          ["Confirmed County" map-us-confirmed-by-county]
                          ["Global Deaths" table-global-deaths]
                          ["Global Recovered" table-global-recovered]
                          ["US Deaths/Recovered" table-us-deaths-recovered]
                          ["US Tested" table-us-tested]
                          ["US Hospitalized" table-us-hospitalized]
                          ["Global Confirmed" line-chart-global-confirmed-linear]
                          ["Global Confirmed" line-chart-global-confirmed-log]
                          ["Global Daily Cases" line-chart-global-daily-cases]]]]]]
           [v-box
            :height "100%"
            :class (str (when (not (nil? @screen)) (name @screen)) " desktop fade-duration-3 " (when @is-loaded "is-active"))
            :children [[:div.fade-duration-2 {:class (if @is-transitioning "is-inactive" "is-active")}
                        [(get map-sub-panels (mod @curr-map (count map-sub-panels)))]]
                       [h-box :class "home-page" :gap gap-size :children
                        [[box :size (if (= @screen :tablet) "2" "220px") :child [home-col-left]]
                         [box :size (if (= @screen :tablet) "4" "auto") :class "home-col-center" :child [map-switcher map-sub-panels]]
                         [box :size (if (= @screen :tablet) "2" "410px") :child [home-col-right]]]]]])])})))
