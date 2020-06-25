(ns covid-dashboard.db)

(def default-db
  {:confirmed-by-province nil
   :confirmed-by-region nil
   :confirmed-by-us-county nil
   :global-deaths nil
   :global-recovered nil
   :is-fetching false
   :time-series-confirmed-global nil
   :total-confirmed nil
   :us-states-deaths-recovered nil})
