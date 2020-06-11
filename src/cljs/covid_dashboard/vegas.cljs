(ns covid-dashboard.vegas
  (:require
   ;; [re-frame.core :as re-frame]
   ;; [re-com.core :as re-com]
   ;; [breaking-point.core :as bp]
   ;; [covid-dashboard.subs :as subs]
   [oz.core :as oz]
   ))


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


(defn line-plot-vega-lite []
  [oz.core/vega-lite line-plot])

(defn map-us-chloropleth-vega []
  [oz.core/vega map-test])
