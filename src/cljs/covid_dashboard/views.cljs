(ns covid-dashboard.views
  (:require [applied-science.js-interop :as j]
            [covid-dashboard.components :refer [detail-and-global-switcher display-and-local-switcher display-detail-menu-switcher]]
            [covid-dashboard.line-charts :as line-charts]
            [covid-dashboard.tables :as tables]
            [breaking-point.core :as bp]
            [covid-dashboard.maps :as maps]
            [covid-dashboard.static :refer [control-bar-height-desktop gap-size duration-2]]
            [covid-dashboard.subs :as subs]
            [covid-dashboard.utility :as utility]
            [re-com.core :refer [box gap h-box v-box]]
            [re-frame.core :refer [dispatch subscribe]]
            [tupelo.core :refer [spyx]]
            [reagent.core :refer [atom create-class with-let]]))

(defn home-col-left []
  [v-box
   :class "home-col-left"
   :gap gap-size
   :size "auto"
   :children [;; panel 1
              [box :class "panel" :child [tables/table-totals]]
              ;; panel 2
              [box :size "1" :class "panel" :child
               [display-and-local-switcher [["Confirmed Country" tables/table-confirmed-country]
                                     ["Confirmed State" tables/table-confirmed-state]
                                     ["Confirmed County" tables/table-confirmed-county]]]]]])

(defn home-col-right []
  [v-box
   :class "home-col-right"
   :gap gap-size
   :size "auto"
   :children [;; panels 4 & 5...
              (if (= @(subscribe [::bp/screen]) :tablet)
                ;; ...tablet - above and below
                [:<>
                 [box :size "1" :class "panel" :child
                  [display-and-local-switcher [["Global Deaths" tables/table-global-deaths]
                                        ["Global Recovered" tables/table-global-recovered]]]]
                 [box :size "1" :class "panel" :child
                  [display-and-local-switcher [["US Deaths/Recovered" tables/table-us-deaths-recovered]
                                        ["US Tested" tables/table-us-tested]
                                        ["US Hospitalized" tables/table-us-hospitalized]]]]]
                ;; ...desktop - side by side
                [box :size "1" :child
                 [h-box :size "1" :gap gap-size :children
                  [[box :size "4" :class "panel" :child
                    [display-and-local-switcher [["Global Deaths" tables/table-global-deaths]
                                          ["Global Recovered" tables/table-global-recovered]]]]
                   [box :size "5" :class "panel" :child
                    [display-and-local-switcher [["US Deaths/Recovered" tables/table-us-deaths-recovered]
                                          ["US Tested" tables/table-us-tested]
                                          ["US Hospitalized" tables/table-us-hospitalized]]]]]]])
              ;; panel 6, same either way
              [box :class "panel svg-pointer-events-none" :size "255px" :child
               [display-and-local-switcher [["Global Confirmed" line-charts/line-chart-global-confirmed-linear]
                                     ["Global Confirmed" line-charts/line-chart-global-confirmed-log]
                                     ["Global Daily Cases" line-charts/line-chart-global-daily-cases]]]]]])

(defn loader []
  (let [is-fetching (subscribe [::subs/is-fetching])]
    [:div.loader.fade-duration-3 {:class (when @is-fetching "is-active")}
     [:div.virion-container
      [:div.virion-container-inner
       [:img.virion {:src "images/virion-sat-fade_500.jpg"}]]]]))

(defn home-page []
  (let [curr-map (subscribe [::subs/curr-map])
        is-loaded (subscribe [::subs/is-loaded])
        is-switching (subscribe [::subs/is-switching])
        map-sub-panels [["US - Confirmed by Population" maps/map-us-confirmed-by-county]
                        ["US - Confirmed by Population" maps/map-us-chloropleth-confirmed-by-county]
                        ["Cumulative Confirmed Cases" maps/map-world-confirmed-by-country]]
        screen (subscribe [::bp/screen])
        is-left-panel-open (atom true)
        is-right-panel-open (atom true)]
    (letfn [(keyboard-listeners [e]
              (let [is-space (= (.-keyCode e) 32)
                    is-left-bracket (= (.-keyCode e) 219)
                    is-right-bracket (= (.-keyCode e) 221)]
                (cond is-space (if @is-left-panel-open
                                 (do (reset! is-left-panel-open false)
                                     (reset! is-right-panel-open false))
                                 (do (reset! is-left-panel-open true)
                                     (reset! is-right-panel-open true)))
                      is-left-bracket (swap! is-left-panel-open not)
                      is-right-bracket (swap! is-right-panel-open not))))]
      (create-class
       {:display-name "home-page"
        :component-did-mount #(.addEventListener js/document "keydown" keyboard-listeners)
        :reagent-render
        (fn [this]
          [:<>
           [loader]
           (if (= @screen :mobile)
             ;; mobile layout
             [v-box
              :height "100%"
              :class (str "fade-duration-3 " (when @is-loaded "is-active"))
              :children [[box :size "1" :class "panel" :child
                          [display-detail-menu-switcher
                           [["Total Confirmed" tables/table-totals]
                            ["Confirmed County" maps/map-us-confirmed-by-county]
                            ["Confirmed County" maps/map-us-chloropleth-confirmed-by-county]
                            ["Confirmed Country" maps/map-world-confirmed-by-country]
                            ["Confirmed Country" tables/table-confirmed-country]
                            ["Confirmed State" tables/table-confirmed-state]
                            ["Confirmed County" tables/table-confirmed-county]
                            ["Global Deaths" tables/table-global-deaths]
                            ["Global Recovered" tables/table-global-recovered]
                            ["US Deaths/Recovered" tables/table-us-deaths-recovered]
                            ["US Tested" tables/table-us-tested]
                            ["US Hospitalized" tables/table-us-hospitalized]
                            ["Global Confirmed" line-charts/line-chart-global-confirmed-linear]
                            ["Global Confirmed" line-charts/line-chart-global-confirmed-log]
                            ["Global Daily Cases" line-charts/line-chart-global-daily-cases]]]]]]
             ;; desktop layout
             [v-box
              :height "100%"
              :class (str (when (not (nil? @screen)) (name @screen)) " desktop fade-duration-3 " (when @is-loaded "is-active"))
              :children [;; map display, full viewport
                         [:div.fade-duration-2 {:class (if @is-switching "is-inactive" "is-active")}
                          [(->> (mod @curr-map (count map-sub-panels)) (get map-sub-panels) second)]]
                         ;; three columns:
                         [h-box :class "home-page" :gap gap-size :children
                          [;; left - panels 1 & 2
                           [box :size (if (= @screen :tablet) (if @is-left-panel-open "2" "0") (if @is-left-panel-open "220px" "0")) :child [home-col-left]]
                           ;; center - panel 3
                           [box :size (if (= @screen :tablet) "4" "auto") :class "home-col-center" :child
                            [detail-and-global-switcher map-sub-panels]]
                           ;; right - panels 4, 5, 6
                           [box :size (if (= @screen :tablet) (if @is-right-panel-open "2" "0") (if @is-right-panel-open "410px" "0")) :child [home-col-right]]]]]])])}))))
