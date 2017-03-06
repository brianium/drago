(ns drago.streams-test
  (:require [cljs.test :refer-macros [deftest is use-fixtures async]]
            [cljs.core.async :refer [<!]]
            [goog.dom :as dom]
            [drago.streams :as streams])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(use-fixtures :once
  {:before (fn []
             (let [element (dom/createElement "div")]
               (set! (.-id element) "clickable")
               (dom/appendChild (.-body js/document) element)))
   :after (fn []
            (let [element (dom/getElement "clickable")]
              (dom/removeNode element)))})

;; defines a message creation function
(defn test-message [event document]
  {:target (.-target event)})

;; defines a stream factory to test
(defonce factory (streams/stream-factory "mousedown" test-message))

;; define a function to dispatch the mousedown event
(defn mousedown [el]
  (let [event (.createEvent js/document "MouseEvent")]
    (.initMouseEvent event
      "mousedown"
      true
      true
      js/window
      nil
      0 0 0 0
      false false false false
      0
      nil)
    (.dispatchEvent el event)))

(deftest receiving-event-messages
  (async done
    (let [ch (factory "#clickable" :begin)]
      (go
        (let [[name {:keys [target]}] (<! ch)]
          (is (= :begin name))
          (is (= "clickable" (.-id target)))
          (done)))
      (mousedown (dom/getElement "clickable")))))
