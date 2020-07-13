(ns covid-dashboard.events
  (:require
   [re-frame.core :as re-frame]
   [covid-dashboard.db :as db]
   [covid-dashboard.config :as config]
   [covid-dashboard.fx :as fx]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [day8.re-frame.http-fx]
   [ajax.core :as ajax]))

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _] db/default-db))

(re-frame/reg-event-db
 ::set-active-page
 (fn-traced [db [_ active-page]] (assoc db :active-page active-page)))

(re-frame/reg-event-db
 :set-detail-country
 (fn-traced [db [_ detail-country]] (assoc db :detail-country detail-country)))

(re-frame/reg-event-db
 :set-detail-county
 (fn-traced [db [_ detail-county]] (assoc db :detail-county detail-county)))

(re-frame/reg-event-db
 :set-detail-state
 (fn-traced [db [_ detail-state]] (assoc db :detail-state detail-state)))

(re-frame/reg-event-db
 :set-detail-value
 (fn-traced [db [_ detail-value]] (assoc db :detail-value detail-value)))

(re-frame/reg-event-db
 :clear-details
 (fn [db [_ detail-value]] (assoc db :detail-county nil :detail-country nil :detail-value nil)))

(re-frame/reg-event-db
 ::assoc-api-all
 (fn [db [_ {:keys [confirmed-by-province
                    confirmed-by-country
                    confirmed-by-us-county
                    confirmed-by-us-county-fips
                    global-deaths
                    global-recovered
                    last-updated
                    time-series-confirmed-global
                    total-confirmed
                    us-states-deaths-recovered
                    us-states-hospitalized
                    us-states-tested]}]]
   (assoc db :confirmed-by-province confirmed-by-province
          :confirmed-by-country confirmed-by-country
          :confirmed-by-us-county confirmed-by-us-county
          :confirmed-by-us-county-fips confirmed-by-us-county-fips
          :global-deaths global-deaths
          :global-recovered global-recovered
          :is-fetching false
          :is-loaded true
          :last-updated last-updated
          :time-series-confirmed-global time-series-confirmed-global
          :total-confirmed total-confirmed
          :us-states-deaths-recovered us-states-deaths-recovered
          :us-states-hospitalized us-states-hospitalized
          :us-states-tested us-states-tested)))

(re-frame/reg-event-db
 ::failure-http-result
 (fn [db [_ result]]
   (assoc db :failure-http-result result)))

(re-frame/reg-event-db
 :tick-event
 (fn [db [_ _]]
   (update db :tick inc)))

(defn api-prefix [x]
  (if config/debug?
    (str "http://localhost:3000/" x)
    (str "/api/" x)))

(re-frame/reg-event-fx
 :call-api-all
 (fn [{:keys [db]} _]
   {:db (assoc db :is-fetching true)
    :http-xhrio {:method :get
                 :uri (api-prefix "all")
                 :timeout 15000
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [::assoc-api-all]
                 :on-failure [::failure-http-result]}}))

(re-frame/reg-event-fx
 :seconds-interval
 (fn [cfx [_ _]]
   {:interval nil}))
