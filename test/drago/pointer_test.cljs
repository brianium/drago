(ns drago.pointer-test
  (:require [cljs.test :refer-macros [deftest async is use-fixtures testing]]
            [cljs.core.async :refer [<! timeout alts!]]
            [goog.dom :as dom]
            [goog.events :as events]
            [drago.test-utils :as utils]
            [drago.pointer :refer [pointer-chan pointer-state]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(use-fixtures :each
  {:before (fn []
             (let [draggable (dom/createElement "div")
                   movable (dom/createElement "div")]
               (utils/append-element draggable "draggable" "square")
               (utils/append-element movable "movable" "mirror")
               (reset! pointer-state {})))
   :after (fn []
            (utils/remove-element "draggable")
            (utils/remove-element "movable")
            (events/removeAll (.-documentElement js/document) "mousemove")
            (events/removeAll (.-documentElement js/document) "mouseup")
            (events/removeAll (.-documentElement js/document) "mousedown"))})

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

(deftest pointer-chan-move-normalized
  (testing "a move event cannot follow a release event"
    (async done
      (let [square (dom/getElement "draggable")
            mirror (dom/getElement "movable")
            doc (.-documentElement js/document)
            ch (pointer-chan)]
        (go-loop [messages []]
          (let [[val _] (alts! [ch (timeout 500)])]
            (when (> (count messages) 2)
              (is (= [:begin :release :begin] messages))
              (done))
            (recur (conj messages (first val)))))
        (go
          (utils/mousedown square)
          (<! (timeout 1))
          (utils/mouseup mirror)
          (<! (timeout 1))
          (utils/mousemove doc)
          (<! (timeout 1))
          (utils/mousedown square))))))
