(ns covid-dashboard.components
  (:require
   [breaking-point.core :as bp]
   [covid-dashboard.static :refer [control-bar-height control-bar-height-desktop duration-2 gap-size]]
   [covid-dashboard.subs :as subs]
   [re-com.core :refer [box gap h-box v-box]]
   [re-frame.core :as re-frame]
   [reagent.core :refer [atom with-let]]))

(defn info-panel
  "Takes is-switching and returns details on the clicked map. Info data is global."
  [is-switching]
  (let [county (re-frame/subscribe [:active-county])
        state (re-frame/subscribe [:active-state])
        country (re-frame/subscribe [:active-country])
        value (re-frame/subscribe [:active-value])]
    [box :class (str "fade-duration-2 " (if (or @county @country) "is-active" "is-inactive"))
     :child (if (or @county @country)
              [:div.panel.info-panel.z-index-1.padding-2.fade-duration-2 {:class (if is-switching "is-inactive" "is-active")}
               [:table.info-table [:tbody
                (when @county [:tr [:td "County: "] [:td.bold @county]])
                (when @state [:tr [:td "State: "] [:td.bold @state]])
                (when @country [:tr [:td "Country: "] [:td.bold @country]])
                [:tr [:td "Confirmed: "] [:td.bold @value]]]]] "")]))

(defn display-and-info-panel-and-local-switcher
  "Takes a vector of title-component pairs, returns a v-box with a display on top and a switcher on bottom.
  The display fades when switching."
  [sub-panels]
  (with-let [sub-panel-count (count sub-panels)
             curr (atom 0) ;; local curr state will be used to display the sub-panels
             is-switching (atom false)
             switch-with-fade #(do (reset! is-switching true)
                                   (js/setTimeout (fn [] (do (re-frame/dispatch [:clear-actives])
                                                             (swap! curr %))) duration-2)
                                   (js/setTimeout (fn [] (reset! is-switching false)) (* duration-2 1.5)))]
    [v-box :size "1" :children
     [;; display
      [box :size "1" :class (str "justify-content-center fade-duration-2 " (if @is-switching "is-inactive" "is-active")) :child [(-> (get sub-panels @curr) second)]]
      ;; info panel
      [info-panel @is-switching]
      ;; switcher
      [box :size (if (= @(re-frame/subscribe [::bp/screen]) :mobile) control-bar-height control-bar-height-desktop) :child
       [h-box :size "1" :class "panel children-align-self-center z-index-1" :children
        [[box :child [:a.button {:on-click #(when (not @is-switching) (switch-with-fade dec))} "←"]]
         [box :size "1" :child [:p.margin-0-auto (-> (get sub-panels (mod @curr sub-panel-count)) first)
                                [:span.light (str " — " (inc (mod @curr sub-panel-count)) "/" sub-panel-count)]]]
         [box :child [:a.button {:on-click #(when (not @is-switching) (switch-with-fade inc))} "→"]]]]]]]))

(defn display-and-local-switcher
  "Takes a vector of title-component pairs, returns a v-box with a display on top and a switcher on bottom.
  Switching state is local."
  [sub-panels]
  (with-let [curr (atom 0) sub-panel-count (count sub-panels)]
    [v-box :size "1" :children
     [;; display
      [box :size "1" :class "justify-content-center" :child [(-> (get sub-panels @curr) second)]]
      ;; switcher
      [box :size (if (= @(re-frame/subscribe [::bp/screen]) :mobile) control-bar-height control-bar-height-desktop) :child
       [h-box :size "1" :class "children-align-self-center z-index-1" :children
        [[box :child [:a.button {:on-click #(reset! curr (if (= (dec @curr) -1) (dec sub-panel-count) (dec @curr)))} "←"]]
         [box :size "1" :child [:p.margin-0-auto (-> (get sub-panels @curr) first) [:span.light (str " — " (inc @curr) "/" sub-panel-count)]]]
         [box :child [:a.button {:on-click #(reset! curr (if (= (inc @curr) sub-panel-count) 0 (inc @curr)))} "→"]]]]]]]))

(defn info-panel-and-global-switcher
  "Takes a vector of title-component vector pairs, returns a v-box with a spacer on top, an info panel
  that shows & hides, and a switcher. Switching state is global."
  [sub-panels]
  (with-let [sub-panel-count (count sub-panels)
             curr-map (re-frame/subscribe [::subs/curr-map]) ;; the maps are displayed
             is-switching (re-frame/subscribe [::subs/is-switching])
             switch-with-fade #(do (re-frame/dispatch [:assoc-is-switching true])
                                   (js/setTimeout (fn [] (do (re-frame/dispatch [:clear-actives])
                                                             (re-frame/dispatch [:update-curr-map %]))) duration-2)
                                   (js/setTimeout (fn [] (re-frame/dispatch [:assoc-is-switching false])) (* duration-2 1.5)))]
    [v-box :size "1" :children
     [;; spacer
      [box :size "1" :child ""]
      ;; info panel
      [info-panel @is-switching]
      ;; switcher
      [box :size control-bar-height-desktop :child
       [h-box :size "1" :class "children-align-self-center z-index-1 panel" :children
        [[box :child [:a.button {:on-click #(when (not @is-switching) (switch-with-fade dec))} "←"]]
         [box :size "1" :child [:p.margin-0-auto (str (->> (mod @curr-map sub-panel-count) (get sub-panels) first)
                                                      " "
                                                      (inc (mod @curr-map sub-panel-count)) "/" sub-panel-count)]]
         [box :child [:a.button {:on-click #(when (not @is-switching) (switch-with-fade inc))} "→"]]]]]]]))
