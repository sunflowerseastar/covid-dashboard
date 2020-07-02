(ns covid-dashboard.db)

(def default-db
  {:confirmed-by-province nil
   :confirmed-by-country nil
   :confirmed-by-us-county nil
   :confirmed-by-us-county-fips nil
   :curr-map 0
   :global-deaths nil
   :global-recovered nil
   :is-fetching false
   :is-loaded false
   :is-transitioning false
   :time-series-confirmed-global nil
   :total-confirmed nil
   :us-states-deaths-recovered nil
   :us-states-hospitalized nil
   :us-states-tested nil})
