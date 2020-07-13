(ns covid-dashboard.tables
  (:require [covid-dashboard.subs :as subs]
            [covid-dashboard.utility :as utility]
            [re-com.core :refer [box gap h-box v-box]]
            [cljs-time.core :as time]
            [cljs-time.coerce :as coerce]
            [cljs-time.format :as format]
            [goog.string :as goog-string]
            [goog.string.format]
            [reagent.core :refer [create-class]]
            [re-frame.core :as re-frame]
            [tupelo.core :refer [it->]]))

(defn table-totals []
  (create-class
   {:component-did-mount #(re-frame/dispatch [:seconds-interval])
    :reagent-render
    (fn [this]
      (let [last-updated (re-frame/subscribe [::subs/last-updated])
            total-confirmed (re-frame/subscribe [::subs/total-confirmed])]
        (when (and @total-confirmed @last-updated)
          (let [server (coerce/from-string @last-updated)
                server-display (format/unparse (format/formatters :rfc822) server)
                local (->> (time/now) time/to-default-time-zone)
                local-display (format/unparse (format/formatter "E, dd MMM YYYY hh:mm A") local)
                interval (time/interval server local)
                tick @(re-frame/subscribe [::subs/tick])
                display-of #(it-> % (js/parseInt it) (mod it 60) (goog-string/format "%02d" it))]
            [:div.u-position-relative.padding-1
             [:h4 "Total Confirmed"] [:h3 (utility/nf @total-confirmed)]
             [:div.text-align-center
              [:h4.extra-margin "Last Updated"]
              [:h5 "server (UTC±00:00)"] [:h6 server-display]
              [:h5 "local time"] [:h6 local-display]
              [:h5 "age (hh:mm:ss)"] [:h6 (str (time/in-days interval) " days, "
                                               (display-of (time/in-hours interval)) ":"
                                               (display-of (time/in-minutes interval)) ":"
                                               (display-of (time/in-seconds interval)))]
              ;; [:a.information-link "information"]
              ]]))))}))

(defn table-confirmed-country []
  (let [confirmed-by-country (re-frame/subscribe [::subs/confirmed-by-country])]
    (when @confirmed-by-country
      [v-box :size "1" :class "white-fade-bottom" :children
       [[box :class "padding-1" :child [:div [:h4 "Confirmed Cases by"] [:h3 "Country/Region"]]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[country value]]
                                [:tr {:key (str country value)}
                                 [:td.bold (utility/nf value)] [:td country]])
                              @confirmed-by-country)]]]]])))

(defn table-confirmed-state []
  (let [confirmed-by-province (re-frame/subscribe [::subs/confirmed-by-province])]
    (when @confirmed-by-province
      [v-box :size "1" :class "white-fade-bottom" :children
       [[box :class "padding-1" :child [:div [:h4 "Confirmed Cases by"] [:h3 "State/Province"]]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[value province country]]
                                [:tr {:key (str province value)} [:td.bold (utility/nf value)] [:td (str province ", " country)]])
                              @confirmed-by-province)]]]]])))

(defn table-confirmed-county []
  (let [confirmed-by-us-county (re-frame/subscribe [::subs/confirmed-by-us-county])]
    (when @confirmed-by-us-county
      [v-box :size "1" :class "white-fade-bottom" :children
       [[box :class "padding-1" :child [:div [:h4 "Confirmed Cases by"] [:h3 "U.S. County"]]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[value us-county state]]
                                [:tr {:key (str us-county value)} [:td.bold (utility/nf value)] [:td (str us-county ", " state)]])
                              @confirmed-by-us-county)]]]]])))

(defn table-global-deaths []
  (let [global-deaths (re-frame/subscribe [::subs/global-deaths])]
    (when-let [{:keys [deaths-by-country total-deaths]} @global-deaths]
      [v-box :size "1" :class "white-fade-bottom" :children
       [[box :class "padding-1" :child [:div [:h4 "Global Deaths"] [:h3 (utility/nf total-deaths)]]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[country value]]
                                [:tr {:key (str country value)} [:td.bold (utility/nf value)] [:td country]])
                              deaths-by-country)]]]]])))

(defn table-global-recovered []
  (let [global-recovered (re-frame/subscribe [::subs/global-recovered])]
    (when-let [{:keys [recovered-by-country total-recovered]} @global-recovered]
      [v-box :size "1" :class "white-fade-bottom" :children
       [[box :class "padding-1" :child [:div [:h4 "Global Recovered"] [:h3 (utility/nf total-recovered)]]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[country value]]
                                [:tr {:key (str country value)} [:td.bold (utility/nf value)] [:td country]])
                              recovered-by-country)]]]]])))

(defn table-us-deaths-recovered []
  (let [us-states-deaths-recovered (re-frame/subscribe [::subs/us-states-deaths-recovered])]
    (when @us-states-deaths-recovered
      [v-box :size "1" :class "white-fade-bottom" :children
       [[box :class "padding-1" :child [:div [:h4 "US State Level"] [:h3 "Deaths, Recovered"]]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[state deaths recovered]]
                                [:tr {:key state} [:td.bold (utility/nf deaths)]
                                 [:td {:class (if recovered "bold" "light")} (if recovered (utility/nf recovered) "n/a")] [:td state]])
                              @us-states-deaths-recovered)]]]]])))

(defn table-us-tested []
  (let [us-states-tested (re-frame/subscribe [::subs/us-states-tested])]
    (when-let [{:keys [tested-by-state total-tested]} @us-states-tested]
      [v-box :size "1" :class "white-fade-bottom" :children
       [[box :class "padding-1" :child [:div [:h4 "US People Tested"] [:h3 (utility/nf total-tested)]]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[state tested]]
                                [:tr {:key state} [:td.bold (utility/nf tested)] [:td state]])
                              tested-by-state)]]]]])))

(defn table-us-hospitalized []
  (let [us-states-hospitalized (re-frame/subscribe [::subs/us-states-hospitalized])]
    (when @us-states-hospitalized
      [v-box :size "1" :class "white-fade-bottom" :children
       [[box :class "padding-1" :child [:div [:h4 "US State Level"] [:h3 "Hospitalizations"]]]
        [box :size "1" :class "scroll-y-auto" :child
         [:table [:tbody (map (fn [[state hospitalized]]
                                [:tr {:key state} [:td.bold (utility/nf hospitalized)] [:td state]])
                              @us-states-hospitalized)]]]]])))
