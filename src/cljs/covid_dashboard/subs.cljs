(ns covid-dashboard.subs
  (:require
   [covid-dashboard.utility :as utility]
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
 ::confirmed-by-country
 (fn [db]
   (:confirmed-by-country db)))

(re-frame/reg-sub
 ::confirmed-by-us-county
 (fn [db]
   (:confirmed-by-us-county db)))

(re-frame/reg-sub
 ::confirmed-by-us-county-fips
 (fn [db]
   (:confirmed-by-us-county-fips db)))

(re-frame/reg-sub
 ::curr-map
 (fn [db]
   (:curr-map db)))

(re-frame/reg-sub
 :detail-country
 (fn [db _]
   (:detail-country db)))

(re-frame/reg-sub
 :detail-county
 (fn [db _]
   (:detail-county db)))

(re-frame/reg-sub
 :detail-state
 (fn [db _]
   (:detail-state db)))

(re-frame/reg-sub
 :detail-value
 (fn [db _]
   (-> (:detail-value db) utility/nf)))

(re-frame/reg-sub
 ::global-deaths
 (fn [db]
   (:global-deaths db)))

(re-frame/reg-sub
 ::global-recovered
 (fn [db]
   (:global-recovered db)))

(re-frame/reg-sub
 ::is-fetching
 (fn [db]
   (:is-fetching db)))

(re-frame/reg-sub
 ::is-loaded
 (fn [db]
   (:is-loaded db)))

(re-frame/reg-sub
 ::is-switching
 (fn [db]
   (:is-switching db)))

(re-frame/reg-sub
 ::last-updated
 (fn [db]
   (:last-updated db)))

(re-frame/reg-sub
 ::tick
 (fn [db]
   (:tick db)))

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

(re-frame/reg-sub
 ::us-states-hospitalized
 (fn [db]
   (:us-states-hospitalized db)))

(re-frame/reg-sub
 ::us-states-tested
 (fn [db]
   (:us-states-tested db)))
