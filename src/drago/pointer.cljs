(ns drago.pointer
  (:require [cljs.core.async :refer [chan >! alts!]]
            [drago.streams :refer [stream-factory]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:import goog.math.Coordinate
           goog.events.BrowserEvent))

(defrecord PointerMessage [point target document])

(defn pointer-message
  "Creates a pointer message containing a coordinate point,
   the event target, and the event document"
  [event document]
  (let [target (.-target event)
        x (.-screenX event)
        y (.-screenY event)
        coords (Coordinate. x y)]
    (->PointerMessage coords target document)))

;;;; Pointer Streams
(defn can-start?
  "Detect if the event is a left click or a touch"
  [event]
  (or (= "touchstart" (.-type event))
      (.isButton event (.. BrowserEvent -MouseButton -LEFT))))

;; Mouse down events are only considered if they are from a left click
(def dragstart
  (stream-factory (array "mousedown" "touchstart") pointer-message can-start?))

(def dragend
  (stream-factory (array "mouseup" "touchend" "touchcancel") pointer-message))

(def dragmove
  (stream-factory (array "mousemove" "touchmove") pointer-message))

(defonce pointer-state (atom {}))

(defn pointer-chan
  "Creates a channel to function as a single stream of
   pointer events - i.e mouse and touch"
  ([{:keys [move-targets]
     :or {move-targets [(.-documentElement js/document)]}}]
   (let [start (dragstart ".square" :begin)
         up (dragend move-targets :release)
         move (dragmove move-targets :move)
         out (chan)]
     (go-loop []
       (let [[data channel] (alts! [start up move])
             message-name (first data)
             last-message (get @pointer-state :last-message)
             state (swap! pointer-state assoc :last-message message-name)]
         (when-not (and (= :move message-name) (= :release last-message))
           (>! out data)))
       (recur))
     out))
  ([]
   (pointer-chan {})))
