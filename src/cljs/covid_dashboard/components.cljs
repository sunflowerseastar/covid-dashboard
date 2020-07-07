(ns covid-dashboard.components
  (:require
   [breaking-point.core :as bp]
   [covid-dashboard.static :refer [control-bar-height control-bar-height-desktop duration-2 gap-size]]
   [covid-dashboard.subs :as subs]
   [re-com.core :refer [box gap h-box v-box]]
   [re-frame.core :as re-frame]
   [reagent.core :refer [atom with-let]]))

(defn detail
  "Takes is-switching and returns details on the clicked map. Detail data is global."
  [is-switching]
  (let [county (re-frame/subscribe [:detail-county])
        state (re-frame/subscribe [:detail-state])
        country (re-frame/subscribe [:detail-country])
        value (re-frame/subscribe [:detail-value])]
    [box :class (str "fade-duration-2 " (if (or @county @country) "is-active" "is-inactive"))
     :child (if (or @county @country)
              [:div.panel.detail.z-index-1.padding-2.fade-duration-2 {:class (if is-switching "is-inactive" "is-active")}
               [:table.detail-table
                [:tbody (when @county [:tr [:td "County: "] [:td.bold @county]])
                 (when @state [:tr [:td "State: "] [:td.bold @state]])
                 (when @country [:tr [:td "Country: "] [:td.bold @country]])
                 [:tr [:td "Confirmed: "] [:td.bold @value]]]]] "")]))

(defn display-detail-menu-switcher
  "Takes a vector of title-component pairs, returns a v-box with, from top to bottom:
  - display
  - detail
  - menu
  - switcher
  The display fades when switching. Switcher state is local. Detail shows after map clicks.
  The menu is a switcher menu that lets the user jump to a sub-panel in the current panel."
  [sub-panels]
  (with-let [sub-panel-count (count sub-panels)
             curr (atom 0) ;; local curr state will be used to display the sub-panels
             is-switching (atom false)
             switch-with-fade #(do (reset! is-switching true)
                                   (js/setTimeout (fn [] (do (re-frame/dispatch [:clear-details])
                                                             (if (and (= % dec) (= (dec @curr) -1))
                                                               (reset! curr (dec sub-panel-count))
                                                               (swap! curr %))
                                                             (reset! is-switching false))) duration-2))]
    [v-box :size "1" :children
     [;; display
      [box :size "1" :class (str "justify-content-center fade-duration-2 " (if @is-switching "is-inactive" "is-active")) :child
       [(-> (get sub-panels (mod @curr sub-panel-count)) second)]]
      ;; detail panel
      [detail @is-switching]
      ;; switcher
      [box :size (if (= @(re-frame/subscribe [::bp/screen]) :mobile) control-bar-height control-bar-height-desktop) :child
       [h-box :size "1" :class "panel children-align-self-center z-index-1" :children
        [[box :child [:a.button {:on-click #(when (not @is-switching) (switch-with-fade dec))} "←"]]
         [box :size "1" :child [:p.margin-0-auto (-> (get sub-panels (mod @curr sub-panel-count)) first)
                                [:span.light (str " — " (inc (mod @curr sub-panel-count)) "/" sub-panel-count)]]]
         [box :child [:a.button {:on-click #(when (not @is-switching) (switch-with-fade inc))} "→"]]]]]]]))

(defn display-and-switcher
  "Takes a vector of title-component pairs, returns a v-box with a display on top and a switcher on bottom.
  Switching state is local."
  [sub-panels]
  (with-let [curr (atom 0)
             is-menu-active (atom false)
             sub-panel-count (count sub-panels)]
    [:div.big-container
     [v-box :size "1" :children
      [;; display
       [box :size "1" :class "justify-content-center" :child [(-> (get sub-panels @curr) second)]]
       ;; switcher
       [box :size (if (= @(re-frame/subscribe [::bp/screen]) :mobile) control-bar-height control-bar-height-desktop) :child
        [h-box :size "1" :attr {:on-click #(do (spyx "hi2")
                                               (spyx is-menu-active)
                                               ;; (reset! is-menu-active true)
                                               (swap! is-menu-active not)
                                               )} :class "children-align-self-center z-index-1" :children
         [[box :child [:a.button {:on-click #(do (.stopPropagation %)
                                                 (reset! curr (if (= (dec @curr) -1) (dec sub-panel-count) (dec @curr))))} "←"]]
          [box :size "1" :child [:p.margin-0-auto (-> (get sub-panels @curr) first) [:span.light (str " — " (inc @curr) "/" sub-panel-count)]]]
          [box :child [:a.button {:on-click #(do (.stopPropagation %)
                                                 (reset! curr (if (= (inc @curr) sub-panel-count) 0 (inc @curr))))} "→"]]]]]]]
     [:div.menu.fade-duration-2 {:class (if @is-menu-active "is-active" "is-inactive")}
      (map-indexed (fn [i [name _]] [:a {:on-click (fn [] (reset! curr i))} name]) sub-panels)
      [:p "hi"]]]))
