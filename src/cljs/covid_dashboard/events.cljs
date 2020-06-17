(ns covid-dashboard.events
  (:require
   [re-frame.core :as re-frame]
   [covid-dashboard.db :as db]
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
 ::assoc-total-confirmed
 (fn [db [_ result]]
   (assoc db :total-confirmed result)))

(re-frame/reg-event-db
 ::failure-http-result
 (fn [db [_ result]]
   (assoc db :failure-http-result result)))

(re-frame/reg-event-fx
 :handler-with-http
 (fn [{:keys [db]} _]
   {:db (assoc db :is-fetching true)
    :http-xhrio {:method :get
                 :uri "http://localhost:3000/total-confirmed"
                 :timeout 8000
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [::assoc-total-confirmed]
                 :on-failure [::failure-http-result]}}))
