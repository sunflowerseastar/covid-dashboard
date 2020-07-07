(ns covid-dashboard.db)

(def default-db
  {:detail-country nil
   :detail-county nil
   :detail-state nil
   :detail-value nil
   :confirmed-by-province nil
   :confirmed-by-country nil
   :confirmed-by-us-county nil
   :confirmed-by-us-county-fips nil
   :global-deaths nil
   :global-recovered nil
   :is-fetching false
   :is-loaded false
   :time-series-confirmed-global nil
   :total-confirmed nil
   :us-states-deaths-recovered nil
   :us-states-hospitalized nil
   :us-states-tested nil})
