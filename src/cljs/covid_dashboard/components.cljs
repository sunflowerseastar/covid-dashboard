(ns covid-dashboard.components
  (:require
   [covid-dashboard.static :refer [gap-size]]
   [covid-dashboard.subs :as subs]
   [re-com.core :refer [box gap h-box v-box]]
   [re-frame.core :as re-frame]
   [reagent.core :refer [atom with-let]]))

(defn sub-panel-container [sub-panels]
  (with-let [curr (atom 0)
             sub-panel-count (count sub-panels)]
    [v-box :size "1" :children
     [[box :size "1" :child [(get sub-panels @curr)]]
      [box :size "40px" :child
       [h-box :size "1" :class "children-align-self-center" :children
        [[gap :size gap-size]
         [box :child [:a {:on-click #(reset! curr (if (= (dec @curr) -1) (dec sub-panel-count) (dec @curr)))} "←"]]
         [box :size "1" :child [:p.margin-0-auto (str "curr " @curr)]]
         [box :child [:a {:on-click #(reset! curr (if (= (inc @curr) sub-panel-count) 0 (inc @curr)))} "→"]]
         [gap :size gap-size]]]]]]))