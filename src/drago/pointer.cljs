(ns drago.pointer
  (:require [cljs.core.async :refer [chan >! alts!]]
            [goog.dom :as dom]
            [goog.dom.classlist :as classes]
            [goog.array :refer [contains]]
            [drago.streams :refer [stream-factory]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:import goog.math.Coordinate
           goog.events.BrowserEvent))

(defrecord PointerMessage [point target])

(defn pointer-message
  "Creates a pointer message containing a coordinate point,
   the event target, and the event document"
  [event _]
  (let [target (.-target event)
        x (.-screenX event)
        y (.-screenY event)
        coords (Coordinate. x y)]
    (->PointerMessage coords target)))

;;;; Pointer Streams
(def begin
  (stream-factory (array "mousedown" "touchstart") pointer-message))

(def release
  (stream-factory (array "mouseup" "touchend" "touchcancel") pointer-message))

(def move
  (stream-factory (array "mousemove" "touchmove") pointer-message))

(def over
  (stream-factory (array "mousemove" "touchmove") pointer-message))

;;;; Stream Filters
(defn- is-left-click-or-touch?
  "Detect if the event is a left click or a touch"
  [event]
  (or (= "touchstart" (.-type event))
    (.isButton event (.. BrowserEvent -MouseButton -LEFT))))

(defn- is-container?
  [container]
  (classes/contains container "drago-container"))

(defn- belongs-to-container?
  [event]
  (let [target (.-target event)
        container (dom/getParentElement target)]
    (is-container? container)))

(def can-start? (every-pred
                  is-left-click-or-touch?
                  belongs-to-container?))

;;;; Global State
(defonce pointer-state (atom {}))

(defn- channels
  "Returns a vector of channels representing drag events"
  [{:keys [frames]
     :or {frames []}}]
  (let [frame-documents (map dom/getFrameContentDocument frames)
        documents (concat [js/document] frame-documents)]
    [(begin documents :begin can-start?)
     (release documents :release)
     (move documents :move)]))

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
