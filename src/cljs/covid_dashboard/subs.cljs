(ns covid-dashboard.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::active-page
 (fn [db _]
   (:active-page db)))

(re-frame/reg-sub
 ::confirmed-by-province
 (fn [db]
   (:confirmed-by-province db)))

(re-frame/reg-sub
 ::confirmed-by-region
 (fn [db]
   (:confirmed-by-region db)))

(re-frame/reg-sub
 ::confirmed-by-us-county
 (fn [db]
   (:confirmed-by-us-county db)))

(re-frame/reg-sub
 ::global-deaths
 (fn [db]
   (:global-deaths db)))

(re-frame/reg-sub
 ::time-series-confirmed-global
 (fn [db]
   (:time-series-confirmed-global db)))

(re-frame/reg-sub
 ::total-confirmed
 (fn [db]
   (:total-confirmed db)))

(re-frame/reg-sub
 ::us-states-deaths-recovered
 (fn [db]
   (:us-states-deaths-recovered db)))
