(ns drago.pointer
  (:require [cljs.core.async :refer [chan >! alts!]]
            [goog.dom :as dom]
            [goog.array :refer [contains]]
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
(def dragstart
  (stream-factory (array "mousedown" "touchstart") pointer-message))

(def dragend
  (stream-factory (array "mouseup" "touchend" "touchcancel") pointer-message))

(def dragmove
  (stream-factory (array "mousemove" "touchmove") pointer-message))

(def mouseover
  (stream-factory "mouseover" pointer-message))

;;;; Stream Filters
(defn- is-left-click-or-touch?
  "Detect if the event is a left click or a touch"
  [[event _]]
  (or (= "touchstart" (.-type event))
    (.isButton event (.. BrowserEvent -MouseButton -LEFT))))

(defn- belongs-to-container?
  [[event containers]]
  (let [target (.-target event)
        container (dom/getParentElement target)]
    (contains containers container)))

(def can-start? (every-pred
                  is-left-click-or-touch?
                  belongs-to-container?))

;;;; Global State
(defonce doc (.-documentElement js/document))
(defonce pointer-state (atom {}))

(defn- channels
  "Returns a vector of channels representing mouse and touch events"
  [{:keys [documents drag-containers]
     :or {documents [doc]
          drag-containers (dom/getElementsByClass "drago-container")}}]
  [(dragstart documents :begin #(can-start? [%1 drag-containers]))
   (dragend documents :release)
   (dragmove documents :move)])

(defn pointer-chan
  "Returns a single channel that receives touch and mouse messages"
  ([config]
   (let [event-channels (channels config) out (chan)]
     (go-loop []
       (let [[data channel] (alts! event-channels)
             message-name (first data)
             last-message (get @pointer-state :last-message)
             state (swap! pointer-state assoc :last-message message-name)]
         (when-not (and (= :move message-name) (= :release last-message))
           (>! out data)))
       (recur))
     out))
  ([]
   (pointer-chan {})))
