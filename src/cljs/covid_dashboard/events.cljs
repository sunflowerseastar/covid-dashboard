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
 ::assoc-api-all
 (fn [db [_ {:keys [confirmed-by-region
                    global-deaths
                    time-series-confirmed-global
                    total-confirmed
                    us-state-level-deaths-recovered]}]]
   (assoc db :confirmed-by-region confirmed-by-region
          :global-deaths global-deaths
          :time-series-confirmed-global time-series-confirmed-global
          :total-confirmed total-confirmed
          :us-state-level-deaths-recovered us-state-level-deaths-recovered)))

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
