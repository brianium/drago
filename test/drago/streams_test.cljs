(ns drago.streams-test
  (:require [cljs.test :refer-macros [deftest is testing]]))

(deftest truth
  (testing "the truth"
    (is (true? true))))
