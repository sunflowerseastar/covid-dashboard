(ns covid-dashboard.db
  (:require [cljs.spec.alpha :as s]))

(s/def ::detail-country (s/nilable string?))
(s/def ::detail-county (s/nilable string?))
(s/def ::detail-state (s/nilable string?))
(s/def ::detail-value (s/nilable int?))
(s/def ::confirmed-by-state (s/nilable (s/cat :confirmed-state-pairs (s/spec (s/cat :confirmed int? :state string?)))))
(s/def ::total-confirmed (s/nilable int?))
(s/def ::tick (s/nilable (s/and int? #(>= % 0))))
(s/def ::db (s/keys :req-un [::total-confirmed ::tick]))

(def default-db
  {:detail-country nil
   :detail-county nil
   :detail-state nil
   :detail-value nil
   :confirmed-by-state nil
   :confirmed-by-country nil
   :confirmed-by-us-county nil
   :confirmed-by-us-county-fips nil
   :global-deaths nil
   :global-recovered nil
   :is-fetching false
   :is-loaded false
   :last-updated nil
   :tick 0
   :time-series-confirmed-global nil
   :total-confirmed nil
   :us-states-deaths-recovered nil
   :us-states-hospitalized nil
   :us-states-tested nil})
