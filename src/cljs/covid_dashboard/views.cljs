(ns covid-dashboard.views
  (:require
   [re-frame.core :as re-frame]
   [re-com.core :as re-com]
   [breaking-point.core :as bp]
   [covid-dashboard.subs :as subs]
   [oz.core :as oz]
   ))


;; home

(defn home-title []
  (let [name (re-frame/subscribe [::subs/name])]
    [re-com/title
     :label (str "Hello from " @name ". This is the Home Page.")
     :level :level1]))

(defn link-to-about-page []
  [re-com/hyperlink-href
   :label "go to About Page"
   :href "#/about"])

(defn home-col-left []
  [re-com/v-box
   :gap "1em"
   :children [[re-com/box :size "1" :child "top"]
              [re-com/box :size "1" :child "bottom"]]])

(defn home-col-right []
  [re-com/v-box
   :gap "1em"
   :children [[re-com/box :size "1" :child "right top"]
              [re-com/box :size "1" :child "right bottom"]]])

(defn group-data [& names]
  (apply concat (for [n names]
                  (map-indexed (fn [i y] {:x i :y y :col n})
                               (take 20 (repeatedly #(rand-int 100)))))))

(def line-plot
  {:data {:values (group-data "monkey" "slipper" "broom" "dragon")}
   :encoding {:x {:field "x"}
              :y {:field "y"}
              :color {:field "col" :type "nominal"}}
   :mark "line"})

(def map-test {:$schema "https://vega.github.io/schema/vega/v5.json"
               :description "A choropleth map depicting U.S. unemployment rates by county in 2009."
               :width 960
               :height 500
               :autosize {:type "fit" :contains "padding"}

               :data [{:name "unemp"
                       :url "https://covid-dashboard.sunflowerseastar.com/data/unemployment.tsv"
                       :format { :type "tsv" :parse "auto"}}
                      {:name "counties"
                       :url "https://covid-dashboard.sunflowerseastar.com/data/us-10m.json"
                       :format { :type "topojson" :feature "counties"}
                       :transform [{:type "lookup" :from "unemp" :key "id" :fields ["id"] :values ["rate"] }
                                   {:type "filter" :expr "datum.rate != null" }]}]

               :projections [{:name "projection" :type "albersUsa"}]

               :scales [{:name "color"
                         :type "quantize"
                         :domain [0 0.15]
                         :range { :scheme "blues" :count 7}}]

               :legends [{:fill "color"
                          :orient "bottom-right"
                          :title "Unemployment"
                          :format "0.1%"}]

               :marks [{:type "shape"
                        :from { :data "counties"}
                        :encode {:enter { :tooltip { :signal "format(datum.rate, '0.1%')" }}
                                 :update {:fill { :scale "color" :field "rate"} }
                                 :hover {:fill { :value "red"} }}
                        :transform [{:type "geoshape" :projection "projection" }]}]})

(defn home-panel []
  [re-com/h-box
   :class "middle"
   :gap "1em"
   :children [[re-com/box :size "1" :child [home-col-left]]
              [re-com/box :size "1"
               :child [:div.container
                       [home-title]
                       [oz.core/vega-lite line-plot]
                       [oz.core/vega map-test]
                       [link-to-about-page]
                       [:div
                        [:h3 (str "screen-width: " @(re-frame/subscribe [::bp/screen-width]))]
                        [:h3 (str "screen: " @(re-frame/subscribe [::bp/screen]))]]]]
              [re-com/box :size "1" :child [home-col-right]]]])


;; about

(defn about-title []
  [re-com/title
   :label "This is the About Page."
   :level :level1])

(defn link-to-home-page []
  [re-com/hyperlink-href
   :label "go to Home Page"
   :href "#/"])

(defn about-panel []
  [re-com/v-box
   :gap "1em"
   :children [[about-title]
              [link-to-home-page]]])


;; main

(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    :about-panel [about-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [re-com/v-box
     :height "100%"
     :children [[panels @active-panel]]]))
