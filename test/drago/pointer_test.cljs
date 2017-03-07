(ns drago.pointer-test
  (:require [cljs.test :refer-macros [deftest async is use-fixtures testing]]
            [cljs.core.async :refer [<!]]
            [goog.dom :as dom]
            [drago.test-utils :as utils]
            [drago.pointer :refer [pointer-chan pointer-state]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(use-fixtures :each
  {:before (fn []
             (let [draggable (dom/createElement "div")
                   movable (dom/createElement "div")]
               (utils/append-element draggable "draggable" "square")
               (utils/append-element movable "movable" "mirror")
               (reset! pointer-state {})))
   :after (fn []
            (utils/remove-element "draggable")
            (utils/remove-element "movable"))})

(deftest pointer-chan-mousedown
  (async done
    (let [square (dom/getElement "draggable")
          ch (pointer-chan)]
      (go
        (let [[name _] (<! ch)]
          (is (= :begin name))
          (done)))
      (utils/mousedown square))))

(deftest pointer-chan-mousemove
  (async done
      (let [ch (pointer-chan)]
        (go
          (let [[name _] (<! ch)]
            (is (= :move name))
            (done)))
        (utils/mousemove (.-documentElement js/document)))))

(deftest pointer-chan-mouseup
  (async done
    (let [mirror (dom/getElement "movable")
          ch (pointer-chan)]
      (go
        (let [[name _] (<! ch)]
          (is (= :release name))
          (done)))
      (utils/mouseup mirror))))
