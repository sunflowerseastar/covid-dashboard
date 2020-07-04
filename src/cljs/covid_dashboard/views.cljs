(ns covid-dashboard.views
  (:require [covid-dashboard.components :refer [sub-panel-container]]
            [covid-dashboard.tables :as tables]
            [breaking-point.core :as bp]
            [covid-dashboard.d3s :as d3s]
            [covid-dashboard.static :refer [control-bar-height-desktop gap-size duration-2]]
            [covid-dashboard.subs :as subs]
            [covid-dashboard.utility :as utility]
            [re-com.core :refer [box gap h-box v-box]]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]))

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
   :children [[box :class "panel" :child [tables/table-totals]]
              [box :size "1" :class "panel" :child
               [sub-panel-container [["Confirmed Country" tables/table-confirmed-country]
                                     ["Confirmed State" tables/table-confirmed-state]
                                     ["Confirmed County" tables/table-confirmed-county]]]]]])

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
                  [sub-panel-container [["Global Deaths" tables/table-global-deaths]
                                        ["Global Recovered" tables/table-global-recovered]]]]
                 [box :size "1" :class "panel" :child
                  [sub-panel-container [["US Deaths/Recovered" tables/table-us-deaths-recovered]
                                        ["US Tested" tables/table-us-tested]
                                        ["US Hospitalized" tables/table-us-hospitalized]]]]]
                ;; ...desktop - side by side
                [box :size "1" :child
                 [h-box :size "1" :gap gap-size :children
                  [[box :size "4" :class "panel" :child
                    [sub-panel-container [["Global Deaths" tables/table-global-deaths]
                                          ["Global Recovered" tables/table-global-recovered]]]]
                   [box :size "5" :class "panel" :child
                    [sub-panel-container [["US Deaths/Recovered" tables/table-us-deaths-recovered]
                                          ["US Tested" tables/table-us-tested]
                                          ["US Hospitalized" tables/table-us-hospitalized]]]]]]])
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
        map-sub-panels [map-us-confirmed-by-county map-world-confirmed-by-country]
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
                         [["Total Confirmed" tables/table-totals]
                          ["Confirmed County" map-us-confirmed-by-county]
                          ["Confirmed Country" map-world-confirmed-by-country]
                          ["Confirmed Country" tables/table-confirmed-country]
                          ["Confirmed State" tables/table-confirmed-state]
                          ["Confirmed County" tables/table-confirmed-county]
                          ["Global Deaths" tables/table-global-deaths]
                          ["Global Recovered" tables/table-global-recovered]
                          ["US Deaths/Recovered" tables/table-us-deaths-recovered]
                          ["US Tested" tables/table-us-tested]
                          ["US Hospitalized" tables/table-us-hospitalized]
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
