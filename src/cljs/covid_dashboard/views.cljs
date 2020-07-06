(ns covid-dashboard.views
  (:require [applied-science.js-interop :as j]
            [covid-dashboard.components :refer [sub-panel-container sub-panel-container-mobile]]
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
              (if (= @(subscribe [::bp/screen]) :tablet)
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
               [sub-panel-container [["Global Confirmed" line-charts/line-chart-global-confirmed-linear]
                                     ["Global Confirmed" line-charts/line-chart-global-confirmed-log]
                                     ["Global Daily Cases" line-charts/line-chart-global-daily-cases]]]]]])

(def curr-map-old (atom 0))

(defn map-world-confirmed-by-country []
  (let [confirmed-by-country (subscribe [::subs/confirmed-by-country])]
    (when @confirmed-by-country
      [:div.u-absolute-all [maps/world-bubble-map @confirmed-by-country]])))

(defn map-us-confirmed-by-county []
  (let [confirmed-by-us-county-fips (subscribe [::subs/confirmed-by-us-county-fips])]
    (when @confirmed-by-us-county-fips
      [:div.u-absolute-all [maps/us-bubble-map @confirmed-by-us-county-fips]])))

(defn map-switcher [sub-panels]
  (with-let [sub-panel-count (count sub-panels)
             curr-map (subscribe [::subs/curr-map])
             is-transitioning (subscribe [::subs/is-transitioning])
             sps ["US - Confirmed by Population" "Cumulative Confirmed Cases"]
             update-map #(do (dispatch [:assoc-is-transitioning true])
                             (js/setTimeout (fn [] (dispatch [:update-curr-map %])) duration-2)
                             (js/setTimeout (fn [] (dispatch [:assoc-is-transitioning false])) (* duration-2 1.5)))]
    [v-box :size "1" :children
     [[box :size "1" :child ""]
      [box :child (let [county (subscribe [:active-county])
                        country (subscribe [:active-country])
                        value (subscribe [:active-value])]
                    (if (or @county @country)
                      [:div.panel.z-index-1.padding-1
                       (when @county [:p [:span.bold "County: "] @county])
                       (when @country [:p [:span.bold "Country: "] @country])
                       [:p [:span.bold "Value: "] @value]] ""))]
      [box :size control-bar-height-desktop :child
       [h-box :size "1" :class "children-align-self-center z-index-1 panel" :children
        [[box :child [:a.button {:on-click #(when (not @is-transitioning) (do (dispatch [:clear-actives])
                                                                              (update-map dec)))} "←"]]
         [box :size "1" :child [:p.margin-0-auto (str (get sps (mod @curr-map sub-panel-count)) " " (inc (mod @curr-map sub-panel-count)) "/" sub-panel-count)]]
         [box :child [:a.button {:on-click #(when (not @is-transitioning) (do (dispatch [:clear-actives])
                                                                              (update-map inc)))} "→"]]]]]]]))

(defn loader []
  (let [is-fetching (subscribe [::subs/is-fetching])]
    [:div.loader.fade-duration-3 {:class (when @is-fetching "is-active")}
     [:div.virion-container
      [:div.virion-container-inner
       [:img.virion {:src "images/virion-sat-fade_500.jpg"}]]]]))

(defn home-page []
  (let [curr-map (subscribe [::subs/curr-map])
        is-loaded (subscribe [::subs/is-loaded])
        is-transitioning (subscribe [::subs/is-transitioning])
        map-sub-panels [map-us-confirmed-by-county map-world-confirmed-by-country]
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
             [v-box
              :height "100%"
              :class (str "fade-duration-3 " (when @is-loaded "is-active"))
              :children [[box :size "1" :class "panel" :child
                          [sub-panel-container-mobile
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
                            ["Global Confirmed" line-charts/line-chart-global-confirmed-linear]
                            ["Global Confirmed" line-charts/line-chart-global-confirmed-log]
                            ["Global Daily Cases" line-charts/line-chart-global-daily-cases]]]]]]
             [v-box
              :height "100%"
              :class (str (when (not (nil? @screen)) (name @screen)) " desktop fade-duration-3 " (when @is-loaded "is-active"))
              :children [[:div.fade-duration-2 {:class (if @is-transitioning "is-inactive" "is-active")}
                          [(get map-sub-panels (mod @curr-map (count map-sub-panels)))]]
                         [h-box :class "home-page" :gap gap-size :children
                          [[box :size (if (= @screen :tablet) (if @is-left-panel-open "2" "0") (if @is-left-panel-open "220px" "0")) :child [home-col-left]]
                           [box :size (if (= @screen :tablet) "4" "auto") :class "home-col-center" :child [map-switcher map-sub-panels]]
                           [box :size (if (= @screen :tablet) (if @is-right-panel-open "2" "0") (if @is-right-panel-open "410px" "0")) :child [home-col-right]]]]]])])}))))
