(ns covid-dashboard.events
  (:require
   [re-frame.core :as re-frame]
   [covid-dashboard.db :as db]
   [covid-dashboard.config :as config]
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
 :set-active-country
 (fn-traced [db [_ active-country]] (assoc db :active-country active-country)))

(re-frame/reg-event-db
 :set-active-county
 (fn-traced [db [_ active-county]] (assoc db :active-county active-county)))

(re-frame/reg-event-db
 :set-active-state
 (fn-traced [db [_ active-state]] (assoc db :active-state active-state)))

(re-frame/reg-event-db
 :set-active-value
 (fn-traced [db [_ active-value]] (assoc db :active-value active-value)))

(re-frame/reg-event-db
 :clear-actives
 (fn [db [_ active-value]] (assoc db :active-county nil :active-country nil :active-value nil)))

(re-frame/reg-event-db
 ::assoc-api-all
 (fn [db [_ {:keys [confirmed-by-province
                    confirmed-by-country
                    confirmed-by-us-county
                    confirmed-by-us-county-fips
                    global-deaths
                    global-recovered
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
          :time-series-confirmed-global time-series-confirmed-global
          :total-confirmed total-confirmed
          :us-states-deaths-recovered us-states-deaths-recovered
          :us-states-hospitalized us-states-hospitalized
          :us-states-tested us-states-tested)))

(re-frame/reg-event-db
 ::failure-http-result
 (fn [db [_ result]]
   (assoc db :failure-http-result result)))

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
                 :timeout 8000
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [::assoc-api-all]
                 :on-failure [::failure-http-result]}}))

(re-frame/reg-event-db
 :assoc-is-switching
 (fn [db [_ bool]]
   (assoc db :is-switching bool)))

(re-frame/reg-event-db
 :assoc-curr-map
 (fn [{:keys [is-switching] :as db} [_ f]]
   (assoc db :curr-map f)))
