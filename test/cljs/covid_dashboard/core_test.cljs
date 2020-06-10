(ns covid-dashboard.core-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [covid-dashboard.core :as core]))

(deftest fake-test
  (testing "fake description"
    (is (= 1 2))))
