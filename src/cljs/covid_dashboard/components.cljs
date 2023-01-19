(ns covid-dashboard.components
  (:require
   [breaking-point.core :as bp]
   [covid-dashboard.static :refer [switcher-height-mobile switcher-height-desktop duration-2 gap-size]]
   [covid-dashboard.subs :as subs]
   [re-com.core :refer [box gap h-box v-box]]
   [re-frame.core :as re-frame]
   [tupelo.core :refer [spyx]]
   [reagent.core :refer [atom with-let]]))

(defn detail
  "Takes is-switching and returns details on the clicked map (hiccup). Detail data is global."
  [is-switching]
  (let [county (re-frame/subscribe [:detail-county])
        state (re-frame/subscribe [:detail-state])
        country (re-frame/subscribe [:detail-country])
        value (re-frame/subscribe [:detail-value])]
    [box :class (str "u-position-relative z-index-1 fade-duration-2 " (if (or @county @country) "is-active" "pointer-events-none"))
     :child (if (or @county @country)
              [:div.panel.detail.z-index-1.padding-2.fade-duration-2 {:class (if is-switching "pointer-events-none" "is-active")}
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
  (with-let [curr (atom 0) ;; local curr state will be used to display the sub-panels
             is-menu-active (atom false)
             is-switching (atom false)
             mobile? (re-frame/subscribe [::bp/mobile?])
             sub-panel-count (count sub-panels)
             switch-with-fade #(do (.stopPropagation %1)
                                   (when (not @is-switching) (reset! is-switching true)
                                         (js/setTimeout (fn [] (do (re-frame/dispatch [:clear-details])
                                                                   (if (and (= %2 dec) (= (dec @curr) -1))
                                                                     (reset! curr (dec sub-panel-count))
                                                                     (swap! curr %2))
                                                                   (reset! is-switching false))) duration-2)))]
    [:div.u-width-100.u-height-100
     [:div.u-position-relative.u-width-100.u-height-100
      ;; display (mobile), detail, & switcher overlay
      [v-box :size "1" :class "u-height-100" :children
       [(if @mobile?
          ;; display (mobile)
          [box :size "1" :class (str "u-width-100 u-height-100 justify-content-center fade-duration-2 " (if @is-switching "pointer-events-none" "is-active"))
           :attr {:on-click #(reset! is-menu-active false)} :child [(-> (get sub-panels (mod @curr sub-panel-count)) second)]]
          ;; spacer
          [box :size "1" :child ""])
        ;; detail
        [detail @is-switching]
        ;; switcher
        [box :size (if (= @(re-frame/subscribe [::bp/screen]) :mobile) switcher-height-mobile switcher-height-desktop) :child
         [h-box :size "1" :attr {:on-click #(swap! is-menu-active not)} :class "panel children-align-self-center z-index-1 cursor-pointer" :children
          [[box :child [:a.button {:on-click #(switch-with-fade % dec)} "←"]]
           [box :size "1" :child [:p.margin-0-auto (-> (get sub-panels (mod @curr sub-panel-count)) first)
                                  [:span.light (str " — " (inc (mod @curr sub-panel-count)) "/" sub-panel-count)]]]
           [box :child [:a.button {:on-click #(switch-with-fade % inc)} "→"]]]]]]]

      ;; menu overlay
      [v-box :size "1" :class "u-absolute-all pointer-events-none" :children
       [[box :size "1" :child ""]
        [box :size "0 1 auto" :child
         [:div.menu-container.pointer-events-auto.fade-duration-2.scroll-y-auto {:class (if @is-menu-active "is-active" "pointer-events-none")}
          [:ul.menu (doall (map-indexed
                            (fn [i [name _ long-name type]]
                              [:li {:key i :class (when (= (mod @curr sub-panel-count) i) "is-selected")
                                    :on-click (fn []
                                                (when (not @is-switching)
                                                  (do (re-frame/dispatch [:clear-details])
                                                      (reset! curr i)
                                                      (reset! is-menu-active false))))}
                               (if @mobile?
                                 [h-box :size "1" :children
                                  [[box :size "5" :class "bold justify-content-flex-end" :child long-name]
                                   [gap :size "10px"]
                                   [box :size "4" :class "light" :child type]]]
                                 [:span name])])
                            sub-panels))]]]
        [box :size (if @mobile? switcher-height-mobile switcher-height-desktop) :child ""]]]]

     ;; display (desktop)
     (when (not @mobile?) [box :size "1" :class (str "u-width-100 u-height-100 justify-content-center fade-duration-2 " (if @is-switching "pointer-events-none" "is-active"))
                           :attr {:on-click #(reset! is-menu-active false)} :child [(-> (get sub-panels (mod @curr sub-panel-count)) second)]])]))

(defn display-and-switcher
  "Takes a vector of title-component pairs, returns a v-box with a display on top, a switcher on bottom, and a menu."
  [sub-panels]
  (with-let [curr (atom 0)
             is-menu-active (atom false)
             sub-panel-count (count sub-panels)
             switch #(do (.stopPropagation %1)
                         (if (and (= %2 dec) (= (dec @curr) -1))
                           (reset! curr (dec sub-panel-count))
                           (swap! curr %2)))]
    [:div.u-position-relative.u-width-100.u-height-100
     [v-box :size "1" :class "u-height-100" :children
      [;; display
       [box :size "1" :class "justify-content-center" :attr {:on-click #(reset! is-menu-active false)} :child
        [(-> (get sub-panels (mod @curr sub-panel-count)) second)]]
       ;; switcher
       [box :size (if (= @(re-frame/subscribe [::bp/screen]) :mobile) switcher-height-mobile switcher-height-desktop) :child
        [h-box :size "1" :attr {:on-click #(swap! is-menu-active not)} :class "children-align-self-center z-index-1 cursor-pointer" :children
         [[box :child [:a.button {:on-click #(switch % dec)} "←"]]
          [box :size "1" :child [:p.margin-0-auto (-> (get sub-panels (mod @curr sub-panel-count)) first)
                                 [:span.light (str " — " (inc (mod @curr sub-panel-count)) "/" sub-panel-count)]]]
          [box :child [:a.button {:on-click #(switch % inc)} "→"]]]]]]]

     [v-box :size "1" :class "u-absolute-all pointer-events-none" :children
      [[box :size "1" :class "blur" :child ""]
       [box :child [:div.menu-container.pointer-events-auto.fade-duration-2 {:class (if @is-menu-active "is-active" "pointer-events-none")}
                    [:ul.menu (doall (map-indexed (fn [i [name]] [:li {:key i :class (when (= (mod @curr sub-panel-count) i) "is-selected")
                                                                       :on-click (fn [] (do (re-frame/dispatch [:clear-details]) (reset! curr i) (reset! is-menu-active false)))}
                                                                  name]) sub-panels))]]]
       [box :size (if (= @(re-frame/subscribe [::bp/screen]) :mobile) switcher-height-mobile switcher-height-desktop) :child ""]]]]))
