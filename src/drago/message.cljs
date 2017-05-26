(ns drago.message
  (:require [goog.dom :as dom])
  (:import goog.math.Coordinate))

(defn pointer
  "Creates a pointer message containing a coordinate point,
   the event target, and the event document"
  [event _]
  (let [target (.-target event)
        screen-x (.-screenX event)
        screen-y (.-screenY event)
        client-x (.-clientX event)
        client-y (.-clientY event)
        point (Coordinate. screen-x screen-y)
        client (Coordinate. client-x client-y)]
    (hash-map :point point :target target :client client)))
