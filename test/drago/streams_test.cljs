(ns drago.streams-test
  (:require [cljs.test :refer-macros [deftest is use-fixtures async testing]]
            [cljs.core.async :refer [<! timeout alts!]]
            [goog.dom :as dom]
            [goog.events :as events]
            [drago.streams :as streams]
            [drago.test-utils :as utils])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(use-fixtures :each
  {:before
   #(async done
     (let [first (dom/createElement "div")]
       (utils/append-element first "clickable" "first")
       (done)))
   :after
   #(async done
      (utils/remove-element "clickable")
      (events/removeAll (.-documentElement js/document) "mousedown")
      (done))})

;; defines a message creation function
(defn test-message [event document]
  {:target (.-target event)})

;; defines a stream filter
(defn left-click? [event]
  (= (.-button event) 0))

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

(deftest filtering-messages
  (testing "events that meet criteria are sent"
    (async done
      (let [ch (factory "#clickable" :begin left-click?)]
        (go
          (let [[val _] (alts! [ch (timeout 500)])
                name (first val)]
            (is (= :begin name))
            (done)))
        (utils/mousedown (dom/getElement "clickable") true 0))))

  (testing "events that do not meet criteria are rejected"
    (async done
      (let [ch (factory "#clickable" :begin left-click?)]
        (go
          (let [[val _] (alts! [ch (timeout 500)])
                name (first val)]
            (is (nil? name))
            (done)))
        (utils/mousedown (dom/getElement "clickable") true 5)))))

