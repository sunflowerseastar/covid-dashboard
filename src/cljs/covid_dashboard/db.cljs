(ns covid-dashboard.db)

(def default-db
  {:confirmed-by-province nil
   :confirmed-by-region nil
   :global-deaths nil
   :is-fetching false
   :time-series-confirmed-global nil
   :total-confirmed nil
   :us-state-level-deaths-recovered nil})
