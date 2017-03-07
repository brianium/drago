(ns drago.streams-test
  (:require [cljs.test :refer-macros [deftest is use-fixtures async]]
            [cljs.core.async :refer [<!]]
            [goog.dom :as dom]
            [drago.streams :as streams]
            [drago.test-utils :as utils])
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

(deftest receiving-event-messages
  (async done
    (let [ch (factory "#clickable" :begin)]
      (go
        (let [[name {:keys [target]}] (<! ch)]
          (is (= :begin name))
          (is (= "clickable" (.-id target)))
          (done)))
      (utils/mousedown (dom/getElement "clickable")))))
