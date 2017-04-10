(ns drago.pointer
  (:require [cljs.core.async :refer [chan >! alts!]]
            [goog.dom :as dom]
            [goog.dom.classlist :as classes]
            [goog.array :refer [contains]]
            [drago.streams :refer [stream-factory]]
            [drago.message :refer [pointer-message move-message]]
            [drago.dom :refer [belongs-to-container?]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:import goog.events.BrowserEvent))

;;;; Pointer Streams
(def begin
  (stream-factory (array "mousedown" "touchstart") pointer-message))

(def release
  (stream-factory (array "mouseup" "touchend" "touchcancel") pointer-message))

(def move
  (stream-factory (array "mousemove" "touchmove") move-message))

;;;; Stream Filters
(defn- is-left-click-or-touch?
  "Detect if the event is a left click or a touch"
  [event]
  (or (= "touchstart" (.-type event))
    (.isButton event (.. BrowserEvent -MouseButton -LEFT))))

(def can-start? (every-pred
                  is-left-click-or-touch?
                  #(belongs-to-container? (.-target %1))))

;;;; Global State
(defonce pointer-state (atom {}))

(defn- update-pointer-state
  "Updates the pointer state atom with relevant message data"
  [[message-name body]]
  (swap! pointer-state assoc :name message-name))

;;;; Channels
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
   (let [event-channels (channels config)
         out (chan)]
     (go-loop []
       (let [[message channel] (alts! event-channels)
             [message-name body] message
             prev-state @pointer-state]
         (update-pointer-state message)
         (when-not (and (= :move message-name) (= :release (:name prev-state)))
           (>! out message)))
       (recur))
     out))
  ([]
   (pointer-chan {})))
