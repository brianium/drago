(ns drago.streams
  "Functions for working with event streams"
  (:require [goog.events :as events]
            [cljs.core.async :as async :refer [chan]]))

(defn- matches?
  "Test if an element matches a css selector"
  [element selector]
  (cond
    (.-matches element) (.matches element selector)
    (.-webkitMatchesSelector element) (.webkitMatchesSelector element selector)
    (.-msMatchesSelector element) (.msMatchesSelector element selector)
    :else false))

(defn- dispatchable?
  "Check if the current event is dispatchable for the given target.

  Target can be a dom element or a selector string"
  [event target pred]
  (let [event-target (.-target event)]
    (if (string? target)
      (and
        (matches? event-target target)
        (pred event))
      (pred event))))

(defn factory
  "Creates a function capable of creating event streams.
  
  'events' is any value accepted by goog.events/listen

  'message-factory' is a function that creates a message 
   structure. It is passed the event and an html document.

   The returned factory accepts a target that can be a CSS selector,
   an html element, or a sequence of elements"
  ([events message-factory]
   (fn stream-factory
     ([target message-name pred document]
      (let [ch (chan)
            selector? (string? target)
            event-targets [(if selector? document target)]]
        (doseq [event-target (flatten event-targets)]
          (events/listen
            event-target
            events
            (fn listener [event]
              (when (dispatchable? event target pred)
                (.preventDefault event)
                (async/put! ch [message-name (message-factory event document)])))))
        ch))

     ([target message-name]
      (stream-factory target message-name some?))
     
     ([target message-name pred]
      (stream-factory target message-name pred js/document)))))
