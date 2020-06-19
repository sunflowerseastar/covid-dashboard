(ns covid-dashboard.core
  (:require
   [breaking-point.core :as bp]
   [covid-dashboard.config :as config]
   [covid-dashboard.events :as events]
   [covid-dashboard.routes :as routes]
   [covid-dashboard.views :as views]
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]
   [reagent.dom :as rdom]))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/home-page] root-el)))

(defn init []
  (routes/app-routes)
  (re-frame/dispatch [:call-api-all])
  (re-frame/dispatch-sync [::events/initialize-db])
  (re-frame/dispatch-sync [::bp/set-breakpoints
                           {:breakpoints [:mobile 768
                                          :tablet 992
                                          :small-monitor 1200
                                          :large-monitor]
                            :debounce-ms 166}])
  (dev-setup)
  (mount-root))
