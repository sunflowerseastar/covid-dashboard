(ns covid-dashboard.maps
  "d3.js visualizations and wrapper components"
  (:require
   [applied-science.js-interop :as j]
   cljsjs.d3
   [breaking-point.core :as bp]
   [covid-dashboard.static :refer [duration-1 duration-2 duration-3]]
   [covid-dashboard.subs :as subs]
   [covid-dashboard.utility :as utility]
   d3-array
   [d3-geo-projection :as d3-geo-projection]
   [d3-geo :as d3-geo]
   [goog.string :as gstring]
   [goog.string.format]
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [topojson-client :as topo]
   [tupelo.core :refer [spyx]]))

(defn us-bubble-map-d3 [svg-el-id width height confirmed-by-us-county-fips]
  (-> (.json js/d3 "data/counties-albers-10m.json")
      (.then
       (fn [counties-albers-10m-data]
         (let [counties-topology (aget counties-albers-10m-data "objects" "counties")
               states-topology (aget counties-albers-10m-data "objects" "states")
               nation-topology (aget counties-albers-10m-data "objects" "nation")
               nation-geojson (topo/feature counties-albers-10m-data nation-topology)
               projection (-> (.geoIdentity js/d3)
                              (.fitExtent (clj->js [[0 0] [(- width 50) (- height 50)]]) nation-geojson))

               path (-> (d3-geo/geoPath) (.projection projection))

               svg (.. js/d3 (select (str "#" svg-el-id)))
               g (-> svg (.append "g"))

               scale-radius (.scaleSqrt js/d3 (clj->js (.extent js/d3 (vals confirmed-by-us-county-fips))) (clj->js [1 40]))

               scale-extent (clj->js [1 12])
               zoom-k->scaled-zoom-k (.scaleLinear js/d3 scale-extent (clj->js [1 0.3]))

               zoomed #(let [transform (-> js/d3 .-event .-transform)
                             k (.-k transform)
                             circles (.selectAll g ".g-circles circle")]
                         (do (-> circles
                                 (.attr "r" (fn [d] (let [scaled-radius (* (zoom-k->scaled-zoom-k k)
                                                                           (scale-radius (.-value d)))]
                                                      (if (js/isNaN scaled-radius) 0 scaled-radius)))))
                             (-> (.select g ".g-circles")
                                 (.attr "stroke-width" (* (zoom-k->scaled-zoom-k k) 0.5)))
                             (-> g (.attr "transform" transform)
                                 (.attr "stroke-width" (/ 1 k)))))
               my-zoom (-> (.zoom js/d3)
                           (.scaleExtent scale-extent)
                           (.on "zoom" zoomed))

               counties-geojson (->> (topo/feature counties-albers-10m-data counties-topology) (.-features))
               counties-geojson-plus-confirmed-values (-> counties-geojson
                                                          (.map #(j/assoc! % :value (get confirmed-by-us-county-fips (keyword (.-id %)))))
                                                          (.filter #(j/get % :value)))]

           (-> svg (.attr "viewBox" (clj->js [0 0 width height])))

           ;; land
           (-> g
               (.append "path")
               (.datum nation-geojson)
               (.join "path")
               (.attr "fill", "#f3f3f300")
               (.attr "d" path)
               (.transition)
               (.duration duration-2)
               (.attr "fill", "#f3f3f3ff"))

           ;; state lines
           (-> g
               (.append "path")
               (.datum (topo/mesh counties-albers-10m-data states-topology (fn [a b] (not= a b))))
               (.attr "fill" "none")
               (.attr "stroke" "#fff")
               (.attr "stroke-linejoin" "round")
               (.attr "d" path))

           ;; marks
           (-> g
               (.append "g")
               (.attr "class" "g-circles")
               (.attr "fill" "#ff8c94")
               (.attr "fill-opacity" 0.5)
               (.attr "stroke" "#fff")
               (.attr "stroke-width" 0.5)

               (.selectAll "circle")
               (.data counties-geojson-plus-confirmed-values)
               (.join
                (fn [enter]
                  (-> enter
                      (.append "circle")
                      (.attr "transform" #(str "translate(" (.centroid path %) ")"))
                      (.attr "r" 0))))
               (.transition)
               (.delay duration-1)
               (.transition)
               (.duration duration-3)
               (.attr "r" (fn [d] (scale-radius (.-value d)))))

           (-> g
               (.selectAll ".g-circles circle")
               (.append "title")
               (.text #(str (-> % .-properties .-name) " – " (utility/nf (.-value %)))))

           (-> g
               (.selectAll ".g-circles circle")
               (.on "mouseover" (fn [d] (this-as this (-> (.select js/d3 this)
                                                          (.filter #(this-as this (-> (.select js/d3 this) (.classed "is-bubble-active") not)))
                                                          (.attr "fill-opacity" 0.5)
                                                          (.transition)
                                                          (.duration 100)
                                                          (.ease #(.easeLinear js/d3 %))
                                                          (.attr "fill-opacity" 0.8)))))
               (.on "mouseout" (fn [d] (this-as this (-> (.select js/d3 this)
                                                         (.filter #(this-as this (-> (.select js/d3 this) (.classed "is-bubble-active") not)))
                                                         (.attr "fill-opacity" 0.8)
                                                         (.transition)
                                                         (.duration 100)
                                                         (.ease #(.easeLinear js/d3 %))
                                                         (.attr "fill-opacity" 0.5)))))
               (.on "click" (fn [d] (this-as this (let [self (.select js/d3 this)
                                                        is-active (-> self (.classed "is-bubble-active"))
                                                        county (j/get-in d [:properties :name])
                                                        value (j/get d :value)]
                                                    (do
                                                      ;; deselect all others
                                                      (-> g (.selectAll ".g-circles circle")
                                                          (.filter #(this-as inner-this (not= this inner-this)))
                                                          (.classed "is-bubble-active" false)
                                                          (.attr "fill-opacity" 0.5))

                                                      (if is-active
                                                        ;; bubble is becoming inactive
                                                        (do
                                                          (re-frame/dispatch [:clear-actives])
                                                          (-> self
                                                              (.classed "is-bubble-active" false)
                                                              (.transition)
                                                              (.duration 500)
                                                              (.attr "fill-opacity" 0.5)))
                                                        ;; bubble is becoming active
                                                        (do
                                                          (re-frame/dispatch [:set-active-county county])
                                                          (re-frame/dispatch [:set-active-value value])
                                                          (-> self
                                                              (.classed "is-bubble-active" true)
                                                              (.transition)
                                                              (.duration 500)
                                                              (.attr "fill-opacity" 1))))))))))

           (-> svg (.call my-zoom)))))))

(defn us-bubble-map [confirmed-by-us-county-fips]
  (let [width @(re-frame/subscribe [::bp/screen-width])
        height @(re-frame/subscribe [::bp/screen-height])
        svg-el-id "us-bubble-map-svg-root"]
    (reagent/create-class
     {:display-name "us-bubble-map-component"
      :component-did-mount (fn [this] (us-bubble-map-d3 svg-el-id width height confirmed-by-us-county-fips))
      :reagent-render (fn [this] [:svg {:id svg-el-id :class "svg-container"}])})))

(defn map-us-confirmed-by-county []
  (let [confirmed-by-us-county-fips (re-frame/subscribe [::subs/confirmed-by-us-county-fips])]
    (when @confirmed-by-us-county-fips
      [:div.u-absolute-all [us-bubble-map @confirmed-by-us-county-fips]])))







(defn us-chloropleth-map-d3 [svg-el-id width height confirmed-by-us-county-fips]
  (-> (.json js/d3 "data/counties-albers-10m.json")
      (.then
       (fn [counties-albers-10m-data]
         (let [counties-topology (aget counties-albers-10m-data "objects" "counties")
               states-topology (aget counties-albers-10m-data "objects" "states")
               nation-topology (aget counties-albers-10m-data "objects" "nation")
               nation-geojson (topo/feature counties-albers-10m-data nation-topology)
               projection (-> (.geoIdentity js/d3)
                              (.fitExtent (clj->js [[0 0] [(- width 50) (- height 50)]]) nation-geojson))

               path (-> (d3-geo/geoPath) (.projection projection))

               svg (.. js/d3 (select (str "#" svg-el-id)))
               g (-> svg (.append "g"))

               scale-radius (.scaleSqrt js/d3 (clj->js (.extent js/d3 (vals confirmed-by-us-county-fips))) (clj->js [1 40]))
               ;; scale-color (.scaleQuantize js/d3 (clj->js [1 10])
               ;;                             (j/get-in js/d3 [:schemeBlues 9]))
               scale-color-2 (-> (.scaleThreshold js/d3)
                                 (.domain (clj->js [0 1 50 100 200 1000 4000 12000]))
                                 (.range (j/get-in js/d3 [:schemeBlues 9])))

               scale-extent (clj->js [1 12])
               zoom-k->scaled-zoom-k (.scaleLinear js/d3 scale-extent (clj->js [1 0.3]))

               zoomed #(let [transform (-> js/d3 .-event .-transform)
                             k (.-k transform)
                             circles (.selectAll g ".g-circles circle")]
                         (do (-> circles
                                 (.attr "r" (fn [d] (let [scaled-radius (* (zoom-k->scaled-zoom-k k)
                                                                           (scale-radius (.-value d)))]
                                                      (if (js/isNaN scaled-radius) 0 scaled-radius)))))
                             (-> (.selectAll g ".counties")
                                 (.attr "stroke-width" (* (zoom-k->scaled-zoom-k k) 0.75)))
                             (-> g (.attr "transform" transform)
                                 (.attr "stroke-width" (/ 1 k)))))
               my-zoom (-> (.zoom js/d3)
                           (.scaleExtent scale-extent)
                           (.on "zoom" zoomed))

               counties-geojson (->> (topo/feature counties-albers-10m-data counties-topology) (.-features))
               counties-geojson-plus-confirmed-values (-> counties-geojson
                                                          (.map #(j/assoc! % :value (get confirmed-by-us-county-fips (keyword (.-id %)))))
                                                          (.filter #(j/get % :value)))

               stroke-color-normal "#fff6"
               stroke-color-active "#fffd"
               stroke-color-hover "#fffa"]

           (-> svg (.attr "viewBox" (clj->js [0 0 width height])))

           ;; land
           (-> g
               (.append "path")
               (.datum nation-geojson)
               (.join "path")
               (.attr "fill", "#f3f3f300")
               (.attr "d" path)
               (.transition)
               (.duration duration-2)
               (.attr "fill", "#f3f3f3ff"))

           ;; marks
           (-> g
               (.append "g")
               (.attr "stroke" stroke-color-normal)
               (.attr "stroke-width" "1px")
               (.attr "stroke-linejoin" "round")
               (.selectAll "path")
               (.data counties-geojson)
               ;; (.join "path")
               (.join (fn [enter]
                        (-> enter
                            (.append "path")
                            (.attr "class" "counties")
                            (.attr "fill" "transparent"))))
               (.transition)
               (.delay duration-2)
               (.transition)
               (.duration duration-2)
               (.attr "fill" (fn [d] (scale-color-2 (j/get d :value))))
               (.attr "d" path))

           (-> g
               (.selectAll ".counties")
               (.on "mouseover" (fn [d] (this-as this (-> (.select js/d3 this)
                                                          (.filter #(this-as this (-> (.select js/d3 this) (.classed "is-county-active") not)))
                                                          (.transition)
                                                          (.duration 100)
                                                          (.attr "stroke" stroke-color-hover)))))
               (.on "mouseout" (fn [d] (this-as this (-> (.select js/d3 this)
                                                         (.filter #(this-as this (-> (.select js/d3 this) (.classed "is-county-active") not)))
                                                         (.transition)
                                                         (.duration 100)
                                                         (.attr "stroke" stroke-color-normal)))))
               (.on "click" (fn [d] (this-as this (let [self (.select js/d3 this)
                                                        is-active (-> self (.classed "is-county-active"))
                                                        county (j/get-in d [:properties :name])
                                                        value (j/get d :value)]
                                                    (do
                                                      ;; deselect all others
                                                      (-> g (.selectAll ".counties")
                                                          (.filter #(this-as inner-this (not= this inner-this)))
                                                          (.classed "is-county-active" false)
                                                          (.transition)
                                                          (.duration 500)
                                                          (.attr "stroke" stroke-color-normal))

                                                      (if is-active
                                                        ;; county is becoming inactive
                                                        (do
                                                          (re-frame/dispatch [:clear-actives])
                                                          (-> self
                                                              (.classed "is-county-active" false)
                                                              (.transition)
                                                              (.duration 500)
                                                              (.attr "stroke" stroke-color-normal)))
                                                        ;; county is becoming active
                                                        (do
                                                          (re-frame/dispatch [:set-active-county county])
                                                          (re-frame/dispatch [:set-active-value value])
                                                          (-> self
                                                              (.classed "is-county-active" true)
                                                              (.transition)
                                                              (.duration 500)
                                                              (.attr "stroke" stroke-color-active))))))))))

           (-> g
               (.selectAll ".counties")
               (.append "title")
               (.text #(str (-> % .-properties .-name) " – " (utility/nf (.-value %)))))

           ;; state lines
           (-> g
               (.append "path")
               (.datum (topo/mesh counties-albers-10m-data states-topology (fn [a b] (not= a b))))
               (.attr "class" "state-lines")
               (.attr "fill" "none")
               (.attr "stroke" stroke-color-normal)
               (.attr "stroke-width" "1px")
               (.attr "stroke-linejoin" "round")
               (.attr "d" path))

           (-> svg (.call my-zoom)))))))

(defn us-chloropleth-map [confirmed-by-us-county-fips]
  (let [width @(re-frame/subscribe [::bp/screen-width])
        height @(re-frame/subscribe [::bp/screen-height])
        svg-el-id "us-chloropleth-map-svg-root"]
    (reagent/create-class
     {:display-name "us-chloropleth-map-component"
      :component-did-mount (fn [this] (us-chloropleth-map-d3 svg-el-id width height confirmed-by-us-county-fips))
      :reagent-render (fn [this] [:svg {:id svg-el-id :class "svg-container"}])})))

(defn map-us-chloropleth-confirmed-by-county []
  (let [confirmed-by-us-county-fips (re-frame/subscribe [::subs/confirmed-by-us-county-fips])]
    (when @confirmed-by-us-county-fips
      [:div.u-absolute-all [us-chloropleth-map @confirmed-by-us-county-fips]])))










(def cross-name-map {"US" "United States of America"})
(defn get-name [name]
  (or (get cross-name-map name) name))

(defn world-bubble-map-d3 [svg-el-id width height world-bubble-map-data]
  (let [data (->> world-bubble-map-data
                  (reduce (fn [acc d] (assoc acc (get-name (first d)) (second d))) {})
                  clj->js)
        svg (-> js/d3 (.select (str "#" svg-el-id)))]
    (-> (.json js/d3 "data/countries-50m.json" #(vector (.-FIPS %) (.-Confirmed %)))
        (.then
         (fn [world]
           (let [countries-geometry-collection (aget world "objects" "countries")
                 countries (topo/feature world countries-geometry-collection)
                 outline (clj->js {"type" "Sphere"})
                 projection (-> (d3-geo/geoNaturalEarth1)
                                (.fitExtent (clj->js [[0.5 0.5] [(- width 0.5) (- height 0.5)]]) outline)
                                (.precision 0.1))
                 path (d3-geo/geoPath projection)
                 g (-> svg (.append "g"))
                 radius (.scaleLinear js/d3 (.extent js/d3 (.values js/Object data)) (clj->js [3 50]))

                 scale-extent (clj->js [1 9])
                 zoom-k->scaled-zoom-k (.scaleSqrt js/d3 scale-extent (clj->js [1 0.22]))

                 zoomed #(let [transform (-> js/d3 .-event .-transform)
                               k (.-k transform)
                               circles (.selectAll g ".g-circles circle")]
                           (do (-> circles
                                   (.attr "r" (fn [d]
                                                (let [scaled-radius (* (zoom-k->scaled-zoom-k k)
                                                                       (radius (aget data (-> d .-properties .-name))))]
                                                  (if (js/isNaN scaled-radius) 0 scaled-radius)))))
                               (-> (.select g ".g-circles")
                                   (.attr "stroke-width" (* (zoom-k->scaled-zoom-k k) 0.5)))
                               (-> g (.attr "transform" transform)
                                   (.attr "stroke-width" (/ 1 k)))))
                 my-zoom (-> (.zoom js/d3)
                             (.scaleExtent scale-extent)
                             (.on "zoom" zoomed))

                 graticule-outline (-> (d3-geo/geoGraticule) (.outline))
                 graticule (-> (d3-geo/geoGraticule10))]

             (-> svg (.attr "viewBox" (clj->js [0 0 width height])))

             ;; land
             (-> g
                 (.append "g")
                 (.selectAll "path")
                 (.data (.-features countries) (fn [d] (-> d .-properties .-name)))
                 (.join (fn [enter]
                          (-> enter
                              (.append "path")
                              (.attr "class" "land")
                              (.attr "fill", "#f3f3f300"))))
                 (.attr "d" path)
                 (.transition)
                 (.duration duration-2)
                 (.attr "fill", "#f3f3f3ff"))

             ;; land title
             (-> g (.selectAll ".land")
                 (.append "title")
                 (.text #(str (-> % .-properties .-name)
                              " – "
                              (utility/nf (aget data (-> % .-properties .-name))))))

             (-> g
                 (.append "path")
                 (.datum graticule-outline)
                 (.attr "class" "graticule")
                 (.attr "d" path))

             (-> g
                 (.append "path")
                 (.datum graticule)
                 (.attr "class" "graticule")
                 (.attr "d" path))

             ;; country borders
             (-> g
                 (.append "path")
                 (.datum (topo/mesh world countries-geometry-collection (fn [a b] (not= a b))))
                 (.attr "fill" "none")
                 (.attr "stroke" "#fff")
                 (.attr "stroke-linejoin" "round")
                 (.attr "d" path))

             ;; marks
             (-> g
                 (.append "g")
                 (.attr "class" "g-circles")
                 (.attr "fill" "#ff8c94")
                 (.attr "fill-opacity" 0.5)
                 (.attr "stroke" "#fff")
                 (.attr "stroke-width" 0.5)
                 (.selectAll "circle")
                 (.data (.-features countries) #(.-id %))
                 (.join (fn [enter]
                          (-> enter
                              (.append "circle")
                              (.attr "transform" (fn [d] (str "translate(" (.centroid path d) ")")))
                              (.attr "r" 0))))
                 (.transition)
                 (.delay duration-1)
                 (.transition)
                 (.duration duration-3)
                 (.attr "r" (fn [d] (radius (aget data (-> d .-properties .-name))))))

             (-> g
                 (.selectAll ".g-circles circle")
                 (.append "title")
                 (.text (fn [d] (str (-> d .-properties .-name) " – "
                                     (utility/nf (aget data (-> d .-properties .-name)))))))

             (-> g
                 (.selectAll ".g-circles circle")
                 (.on "mouseover" (fn [d] (this-as this (-> (.select js/d3 this)
                                                            (.filter #(this-as this (-> (.select js/d3 this) (.classed "is-bubble-active") not)))
                                                            (.attr "fill-opacity" 0.5)
                                                            (.transition)
                                                            (.duration 100)
                                                            (.ease #(.easeLinear js/d3 %))
                                                            (.attr "fill-opacity" 0.8)))))
                 (.on "mouseout" (fn [d] (this-as this (-> (.select js/d3 this)
                                                           (.filter #(this-as this (-> (.select js/d3 this) (.classed "is-bubble-active") not)))
                                                           (.attr "fill-opacity" 0.8)
                                                           (.transition)
                                                           (.duration 100)
                                                           (.ease #(.easeLinear js/d3 %))
                                                           (.attr "fill-opacity" 0.5)))))
                 (.on "click" (fn [d] (this-as this (let [self (.select js/d3 this)
                                                          is-active (-> self (.classed "is-bubble-active"))
                                                          country (j/get-in d [:properties :name])
                                                          value (j/get data country)]
                                                      (do
                                                        ;; deselect all others
                                                        (-> g (.selectAll ".g-circles circle")
                                                            (.filter #(this-as inner-this (not= this inner-this)))
                                                            (.classed "is-bubble-active" false)
                                                            (.attr "fill-opacity" 0.5))

                                                        (if is-active
                                                          ;; bubble is becoming inactive
                                                          (do
                                                            (re-frame/dispatch [:clear-actives])
                                                            (-> self
                                                                (.classed "is-bubble-active" false)
                                                                (.transition)
                                                                (.duration 500)
                                                                (.attr "fill-opacity" 0.5)))
                                                          ;; bubble is becoming active
                                                          (do
                                                            (re-frame/dispatch [:set-active-country country])
                                                            (re-frame/dispatch [:set-active-value value])
                                                            (-> self
                                                                (.classed "is-bubble-active" true)
                                                                (.transition)
                                                                (.duration 500)
                                                                (.attr "fill-opacity" 1))))))))))

             (-> svg (.call my-zoom))))))))

(defn world-bubble-map [line-chart-data]
  (let [width @(re-frame/subscribe [::bp/screen-width])
        height @(re-frame/subscribe [::bp/screen-height])
        svg-el-id "world-bubble-map-root-svg"]
    (reagent/create-class
     {:display-name "world-bubble-map"
      :component-did-mount #(world-bubble-map-d3 svg-el-id width height line-chart-data)
      :reagent-render (fn [this] [:svg {:id svg-el-id :class "svg-container"}])})))

(defn map-world-confirmed-by-country []
  (let [confirmed-by-country (re-frame/subscribe [::subs/confirmed-by-country])]
    (when @confirmed-by-country
      [:div.u-absolute-all [world-bubble-map @confirmed-by-country]])))
