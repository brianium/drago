(ns drago.pointer-test
  (:require [cljs.test :refer-macros [deftest async is use-fixtures testing]]
            [cljs.core.async :refer [<! timeout alts!]]
            [goog.dom :as dom]
            [goog.dom.classlist :as classes]
            [goog.events :as events]
            [drago.config :as config]
            [drago.test-utils :as utils]
            [drago.pointer :refer [pointer-chan pointer-state]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(use-fixtures :each
  {:before
   #(async done
      (let [container (dom/createElement "div")
            draggable (dom/createElement "div")
            movable (dom/createElement "div")]
        (utils/append-element container "container" "drago-container")
        (set! (.-id draggable) "draggable")
        (dom/appendChild container draggable)
        (utils/append-element movable "movable" "mirror")
        (reset! pointer-state {})
        (done)))
   :after
   #(async done
     (utils/remove-element "container")
     (utils/remove-element "movable")
     (events/removeAll (.-documentElement js/document) "mousemove")
     (events/removeAll (.-documentElement js/document) "mouseup")
     (events/removeAll (.-documentElement js/document) "mousedown")
     (done))})

(defn- state-atom []
  (atom {:config (config/create)}))

(deftest pointer-chan-down
  (async done
    (let [square (dom/getElement "draggable")
          ch (pointer-chan (state-atom))]
      (go
        (let [[name _] (<! ch)]
          (is (= :begin name))
          (done)))
      (utils/mousedown square))))

(deftest pointer-chan-move
  (testing "the document element sends mousemove events"
    (async done
      (let [ch (pointer-chan (state-atom))]
        (go
          (let [[name _] (<! ch)]
            (is (= :move name))
            (done)))
        (utils/mousemove (.-documentElement js/document))))))

;;; todo - fix this test now that elementFromPoint is used instead of event target
(comment (deftest pointer-chan-leave
           (testing "moving off a container sends leave message"
             (async done
               (let [ch (pointer-chan (state-atom))
                     container (dom/getElement "container")]
                 (go
                   (let [[name _] (<! ch)]
                     (is (= :move name))))

                 (go
                   (let [[name _] (<! ch)]
                     (is (= :leave name))
                     (done)))

                 (go
                   (utils/mousemove container)
                   (<! (timeout 1))
                   (utils/mousemove (.-documentElement js/document))))))))

(deftest pointer-chan-release
  (async done
    (let [mirror (dom/getElement "movable")
          ch (pointer-chan (state-atom))]
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
            ch (pointer-chan (state-atom))]
        (go
          (let [[val _] (alts! [ch (timeout 500)])]
            (is (= :begin (first val)))))

        (go
          (let [[val _] (alts! [ch (timeout 500)])]
            (is (= :release (first val)))))

        (go
          (let [[val _] (alts! [ch (timeout 500)])]
            (is (= :begin (first val)))
            (done)))

        ; fire events in sequence
        (go
          (utils/mousedown square)
          (<! (timeout 1))
          (utils/mouseup mirror)
          (<! (timeout 1))
          (utils/mousemove doc)
          (<! (timeout 1))
          (utils/mousedown square))))))
