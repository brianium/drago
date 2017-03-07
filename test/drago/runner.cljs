(ns drago.runner
  (:require [doo.runner :refer-macros [doo-all-tests]]
            [drago.streams-test]
            [drago.reduce-test]
            [drago.pointer-test]))

(doo-all-tests #"(drago)\..*-test")
