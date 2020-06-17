(ns covid-dashboard.events
  (:require
   [re-frame.core :as re-frame]
   [covid-dashboard.db :as db]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [day8.re-frame.http-fx]
   [ajax.core :as ajax]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
   db/default-db))

(re-frame/reg-event-db
 ::set-active-page
 (fn-traced [db [_ active-page]]
   (assoc db :active-page active-page)))

(re-frame/reg-event-db
 ::success-http-result
 (fn [db [_ result]]
   (assoc db :success-http-result result)))

(re-frame/reg-event-db
 ::failure-http-result
 (fn [db [_ result]]
   ;; result is a map containing details of the failure
   (assoc db :failure-http-result result)))

(re-frame/reg-event-fx                             ;; note the trailing -fx
 :handler-with-http                      ;; usage:  (dispatch [:handler-with-http])
 (fn [{:keys [db]} _]                    ;; the first param will be "world"
   {:db   (assoc db :show-twirly true)   ;; causes the twirly-waiting-dialog to show??
    :http-xhrio {:method          :get
                 :uri             "https://api.github.com/orgs/day8"
                 :timeout         8000                                           ;; optional see API docs
                 :response-format (ajax/json-response-format {:keywords? true})  ;; IMPORTANT!: You must provide this.
                 :on-success      [::success-http-result]
                 :on-failure      [::failure-http-result]}}))
