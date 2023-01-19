(ns covid-dashboard.views
  (:require [applied-science.js-interop :as j]
            [covid-dashboard.components :refer [display-and-switcher display-detail-menu-switcher]]
            [covid-dashboard.line-charts :as line-charts]
            [covid-dashboard.tables :as tables]
            [breaking-point.core :as bp]
            [covid-dashboard.maps :as maps]
            [covid-dashboard.static :refer [duration-2 gap-size switcher-height-desktop]]
            [covid-dashboard.subs :as subs]
            [covid-dashboard.utility :as utility]
            [re-com.core :refer [box gap h-box v-box]]
            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :refer [atom create-class with-let]]
            [tupelo.core :refer [spyx]]))

(def mobile-panels [["Total Confirmed" tables/table-totals "Overview" ""]
                    ["Confirmed US County" maps/map-us-confirmed-by-county "Confirmed by US County" "US map - bubble"]
                    ["Confirmed US County (2)" maps/map-us-chloropleth-confirmed-by-county "Confirmed by US County" "US map - chloropleth"]
                    ["Confirmed Country" maps/map-world-confirmed-by-country "Confirmed by Country" "world map - bubble"]
                    ["Confirmed Country" tables/table-confirmed-country "Confirmed by Country" "table"]
                    ["Confirmed State" tables/table-confirmed-state "Confirmed by Country" "table"]
                    ["Confirmed US County" tables/table-confirmed-county "Confirmed by US County" "table"]
                    ["Global Deaths" tables/table-global-deaths "Global Deaths" "table"]
                    ;; ["Global Recovered" tables/table-global-recovered "Global Recovered" "table"]
                    ["US Deaths/Recovered" tables/table-us-deaths-recovered "US Deaths & Recovered" "table"]
                    ;; ["US Tested" tables/table-us-tested "US Tested" "table"]
                    ;; ["US Hospitalized" tables/table-us-hospitalized "US Hospitalized" "table"]
                    ["Global Confirmed" line-charts/line-chart-global-confirmed-linear "Global Confirmed" "line chart - linear"]
                    ["Global Confirmed - log" line-charts/line-chart-global-confirmed-log "Global Confirmed" "line chart - log"]
                    ["Global Daily Cases" line-charts/line-chart-global-daily-cases "Global Daily Cases" "line chart - linear"]])

(defn home-col-left []
  [v-box
   :class "home-col-left"
   :gap gap-size
   :size "auto"
   :children [;; panel 1
              [box :class "panel" :child [tables/table-totals]]
              ;; panel 2
              [box :size "1" :class "panel" :child
               [display-and-switcher [["Confirmed Country" tables/table-confirmed-country]
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
                  [display-and-switcher [["Global Deaths" tables/table-global-deaths]
                                         ;; ["Global Recovered" tables/table-global-recovered]
                                         ]]]
                 [gap :size gap-size]
                 [box :size "1" :class "panel" :child
                  [display-and-switcher [["US Deaths/Recovered" tables/table-us-deaths-recovered]
                                         ;; ["US Tested" tables/table-us-tested]
                                         ;; ["US Hospitalized" tables/table-us-hospitalized]
                                         ]]]]
                ;; ...desktop - side by side
                [box :size "1" :child
                 [h-box :size "1" :gap gap-size :children
                  [[box :size "4" :class "panel" :child
                    [display-and-switcher [["Global Deaths" tables/table-global-deaths]
                                           ;; ["Global Recovered" tables/table-global-recovered]
                                           ]]]
                   [box :size "5" :class "panel" :child
                    [display-and-switcher [["US Deaths/Recovered" tables/table-us-deaths-recovered]
                                           ;; ["US Tested" tables/table-us-tested]
                                           ;; ["US Hospitalized" tables/table-us-hospitalized]
                                           ]]]]]])
              ;; panel 6, same either way
              [box :class "panel svg-pointer-events-none" :size "255px" :child
               [display-and-switcher [["Global Confirmed" line-charts/line-chart-global-confirmed-linear]
                                      ["Global Confirmed" line-charts/line-chart-global-confirmed-log]
                                      ["Global Daily Cases" line-charts/line-chart-global-daily-cases]]]]]])

(defn loader []
  (let [is-fetching (subscribe [::subs/is-fetching])]
    ;; loader has a css keyframe animation to fade in - see css.clj
    [:div.loader {:class (when (not @is-fetching) "pointer-events-none")}
     ;; then .virion-container controls the fade-out
     [:div.virion-container.opacity-0-fade-duration-2 {:class (when @is-fetching "opacity-1")}
      [:div.virion-container-inner
       [:img.virion {:src "images/virion-sat-fade_500.jpg"}]]]]))

(defn home-page []
  (let [is-loaded (subscribe [::subs/is-loaded])
        map-sub-panels [["Confirmed US County" maps/map-us-confirmed-by-county]
                        ["Confirmed US County (2)" maps/map-us-chloropleth-confirmed-by-county]
                        ["Global Confirmed Cases" maps/map-world-confirmed-by-country]]
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
              :class (str "opacity-0-fade-duration-3 " (when @is-loaded "opacity-1"))
              :children [[box :size "1" :class "panel" :child [display-detail-menu-switcher mobile-panels]]]]
             ;; desktop layout
             [v-box
              :height "100%"
              :class (str (when (not (nil? @screen)) (name @screen)) " desktop opacity-0-fade-duration-3 " (when @is-loaded "opacity-1"))
              :children [;; three columns:
                         [h-box :class "home-page" :gap gap-size :children
                          [;; left - panels 1 & 2
                           [box :size (if (= @screen :tablet) (if @is-left-panel-open "2" "0") (if @is-left-panel-open "220px" "0")) :child [home-col-left]]
                           ;; center - panel 3
                           [box :size (if (= @screen :tablet) "4" "auto") :class "home-col-center" :child
                            [display-detail-menu-switcher map-sub-panels]]
                           ;; right - panels 4, 5, 6
                           [box :size (if (= @screen :tablet) (if @is-right-panel-open "2" "0") (if @is-right-panel-open "410px" "0")) :child [home-col-right]]]]]])
           [:div.info
            [:h4 "COVID-19 Dashboard"]
            [:p "Inspired by the excellent " [:a {:href "https://coronavirus.jhu.edu/map.html"} "COVID-19 Dashboard by the Center for Systems Science and Engineering (CSSE) at Johns Hopkins University"] "."]
            [:p "This dashboard uses data aggregated by CSSE available here."]
            [:p "Code is open source and available " [:a {:href "https://github.com/sunflowerseastar.com/covid-dashboard"} "here"] "."]
            ]])}))))
