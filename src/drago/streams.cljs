(ns drago.streams
  "Functions for working with event streams"
  (:require [goog.events :as events]
            [cljs.core.async :refer [chan put!]]))

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
