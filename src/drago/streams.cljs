(ns drago.streams
  "Functions for working with event streams"
  (:require [goog.events :as events]
            [cljs.core.async :refer [chan put!]]))

(defn- dispatchable?
  "Check if the current event is dispatchable for the given target.

  target can be a dom element or a selector string"
  [event target]
  (let [event-target (.-target event)]
    (if (string? target)
      (.matches event-target target)
      true)))

(defn stream-factory
  "Creates a function capable of creating event streams.
  
  'events' is a any value acceptable by goog.events/listen

  'message-factory' is a function that creates a message 
   structure. It is passed the event and an html document.

   The returned factory accepts a target that can be a CSS selector,
   an html element, or a sequence of elements"
  [events message-factory]
  (fn factory
    ([target message-name document]
     (let [ch (chan)
           selector? (string? target)
           event-targets [(if selector? document target)]]
       (doseq [event-target (flatten event-targets)]
         (events/listen
           event-target
           events
           (fn listener [event]
             (.preventDefault event)
             (when (dispatchable? event target)
               (put! ch [message-name (message-factory event document)]))())))
       ch))
    ([target message-name]
     (factory target message-name js/document))))
