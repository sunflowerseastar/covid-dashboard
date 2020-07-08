(ns covid-dashboard.fx
  (:require [re-frame.core :as re-frame]))

(defonce timeouts (atom nil))

(re-frame/reg-fx
 :interval
 (fn [_]
   (when (nil? @timeouts)
     (reset! timeouts (js/setInterval (fn [] (re-frame/dispatch [:tick-event])) 1000)))))
