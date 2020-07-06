(ns covid-dashboard.components
  (:require
   [breaking-point.core :as bp]
   [covid-dashboard.static :refer [control-bar-height control-bar-height-desktop duration-2 gap-size]]
   [covid-dashboard.subs :as subs]
   [re-com.core :refer [box gap h-box v-box]]
   [re-frame.core :as re-frame]
   [reagent.core :refer [atom with-let]]))

(defn sub-panel-container-mobile
  "Takes a vector of title-component pairs, returns a fading v-box 'switcher' panel"
  [sub-panels]
  (with-let [curr (atom 0) is-transitioning (atom false) sub-panel-count (count sub-panels)
             update-map #(do (reset! is-transitioning true)
                             (js/setTimeout (fn [] (swap! curr %)) duration-2)
                             (js/setTimeout (fn [] (reset! is-transitioning false)) (* duration-2 1.5)))]
    [v-box :size "1" :children
     [[box :size "1" :class (str "justify-content-center fade-duration-2 " (if @is-transitioning "is-inactive" "is-active")) :child [(-> (get sub-panels @curr) second)]]
      [box :size (if (= @(re-frame/subscribe [::bp/screen]) :mobile) control-bar-height control-bar-height-desktop) :child
       [h-box :size "1" :class "children-align-self-center z-index-1" :children
        [[box :child [:a.button {:on-click #(when (not @is-transitioning) (update-map dec))} "←"]]
         [box :size "1" :child [:p.margin-0-auto (-> (get sub-panels (mod @curr sub-panel-count)) first)
                                [:span.light (str " — " (inc (mod @curr sub-panel-count)) "/" sub-panel-count)]]]
         [box :child [:a.button {:on-click #(when (not @is-transitioning) (update-map inc))} "→"]]]]]]]))

(defn sub-panel-container
  "Takes a vector of title-component pairs, returns a v-box 'switcher' panel"
  [sub-panels]
  (with-let [curr (atom 0) sub-panel-count (count sub-panels)]
    [v-box :size "1" :children
     [[box :size "1" :class "justify-content-center" :child [(-> (get sub-panels @curr) second)]]
      [box :size (if (= @(re-frame/subscribe [::bp/screen]) :mobile) control-bar-height control-bar-height-desktop) :child
       [h-box :size "1" :class "children-align-self-center z-index-1" :children
        [[box :child [:a.button {:on-click #(reset! curr (if (= (dec @curr) -1) (dec sub-panel-count) (dec @curr)))} "←"]]
         [box :size "1" :child [:p.margin-0-auto (-> (get sub-panels @curr) first) [:span.light (str " — " (inc @curr) "/" sub-panel-count)]]]
         [box :child [:a.button {:on-click #(reset! curr (if (= (inc @curr) sub-panel-count) 0 (inc @curr)))} "→"]]]]]]]))

(defn map-switcher [sub-panels]
  (with-let [sub-panel-count (count sub-panels)
             curr-map (re-frame/subscribe [::subs/curr-map])
             is-transitioning (re-frame/subscribe [::subs/is-transitioning])
             update-map #(do (re-frame/dispatch [:assoc-is-transitioning true])
                             (js/setTimeout (fn [] (re-frame/dispatch [:update-curr-map %])) duration-2)
                             (js/setTimeout (fn [] (re-frame/dispatch [:assoc-is-transitioning false])) (* duration-2 1.5)))]
    [v-box :size "1" :children
     [[box :size "1" :child ""]
      [box :child (let [county (re-frame/subscribe [:active-county])
                        country (re-frame/subscribe [:active-country])
                        value (re-frame/subscribe [:active-value])]
                    (if (or @county @country)
                      [:div.panel.z-index-1.padding-1
                       (when @county [:p [:span.bold "County: "] @county])
                       (when @country [:p [:span.bold "Country: "] @country])
                       [:p [:span.bold "Value: "] @value]] ""))]
      [box :size control-bar-height-desktop :child
       [h-box :size "1" :class "children-align-self-center z-index-1 panel" :children
        [[box :child [:a.button {:on-click #(when (not @is-transitioning) (do (re-frame/dispatch [:clear-actives])
                                                                              (update-map dec)))} "←"]]
         [box :size "1" :child [:p.margin-0-auto (str (->> (mod @curr-map sub-panel-count) (get sub-panels) first)
                                                      " "
                                                      (inc (mod @curr-map sub-panel-count)) "/" sub-panel-count)]]
         [box :child [:a.button {:on-click #(when (not @is-transitioning) (do (re-frame/dispatch [:clear-actives])
                                                                              (update-map inc)))} "→"]]]]]]]))
