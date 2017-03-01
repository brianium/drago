(ns drago.pointer
  (:require [goog.events :as events]
            [cljs.core.async :refer [chan put! <! >! alts!]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:import goog.math.Coordinate))

(defn- matches
  "Check if the given event's target matches the selector"
  [event selector]
  (let [target (.-target event)]
    (.matches target selector)))

(defn stream-factory
  "Creates a function capable of creating event streams.
  
  'events' is a any value acceptable by goog.events/listen

  'message-factory' is a function that creates a message 
   structure. It is passed the event and an html document"
  [events message-factory]
  (fn factory
    ([selector message-name document]
     (let [ch (chan)]
       (events/listen
         document
         events
         (fn listener [event]
           (when (matches event selector)
             (.preventDefault event)
             (put! ch [message-name (message-factory event document)]))))
       ch))
    ([selector message-name]
     (factory selector message-name js/document))))

(defrecord PointerMessage [point target document])

(defn mouse-message
  [event document]
  (let [target (.-target event)
        x (.-clientX event)
        y (.-clientY event)
        coords (Coordinate. x y)]
    (->PointerMessage coords target document)))

(def mousedown
  (stream-factory "mousedown" mouse-message))

(def mouseup
  (stream-factory "mouseup" mouse-message))

(def mousemove
  (stream-factory "mousemove" mouse-message))

(defonce pointer-state (atom {}))

(defn pointer-chan []
  (let [down (mousedown ".square" "begin")
        up (mouseup ".mirror" "release")
        move (mousemove ".mirror" "move")
        out (chan)]
    (go-loop []
      (let [[data channel] (alts! [down up move])
            message-name (first data)
            last-message (get @pointer-state :last-message)
            state (swap! pointer-state assoc :last-message message-name)]
        (when-not (and (= "move" message-name) (= "release" last-message))
          (>! out data)))
      (recur))
    out))
