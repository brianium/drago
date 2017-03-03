(ns drago.pointer
  (:require [cljs.core.async :refer [chan >! alts!]]
            [drago.streams :refer [stream-factory]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:import goog.math.Coordinate))

(defrecord PointerMessage [point target document])

(defn pointer-message
  "Creates a pointer message containing a coordinate point,
   the event target, and the event document"
  [event document]
  (let [target (.-target event)
        x (.-clientX event)
        y (.-clientY event)
        coords (Coordinate. x y)]
    (->PointerMessage coords target document)))

;;;; Pointer Streams
(def mousedown
  (stream-factory "mousedown" pointer-message))

(def mouseup
  (stream-factory "mouseup" pointer-message))

(def mousemove
  (stream-factory "mousemove" pointer-message))

;;; Pointer chan is a single stream of event data for
;;; all pointer events - that is mouse and touch
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
