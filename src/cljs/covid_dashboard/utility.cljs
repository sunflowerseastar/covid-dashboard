(ns covid-dashboard.utility
  (:require
   [goog.i18n.NumberFormat.Format])
  (:import
   (goog.i18n NumberFormat)
   (goog.i18n.NumberFormat Format)))

(def nff
  (NumberFormat. Format/DECIMAL))

(defn- nf
  [num]
  (.format nff (str num)))
