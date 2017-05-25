(ns drago.pointer
  (:require [cljs.core.async :refer [chan >! alts!]]
            [goog.dom :as dom]
            [goog.dom.classlist :as classes]
            [goog.array :refer [contains]]
            [drago.streams :refer [stream-factory]]
            [drago.message :refer [pointer-message]]
            [drago.dnd.message :refer [drag-message]]
            [drago.dnd.container :refer [belongs-to-container?]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:import goog.events.BrowserEvent))

;;;; Pointer Streams
(def begin
  (stream-factory (array "mousedown" "touchstart") pointer-message))

(def release
  (stream-factory (array "mouseup" "touchend" "touchcancel") pointer-message))

(def move
  (stream-factory (array "mousemove" "touchmove") drag-message))

;;;; Stream Filters
(defn- is-left-click-or-touch?
  "Detect if the event is a left click or a touch"
  [{:keys [event]}]
  (or (= "touchstart" (.-type event))
    (.isButton event (.. BrowserEvent -MouseButton -LEFT))))

(defn- in-container?
  "Only elements within containers can be dragged"
  [{:keys [event containers]}]
  (let [target (.-target event)]
    (belongs-to-container? containers target)))

(def can-start? (every-pred
                  is-left-click-or-touch?
                  in-container?))

;;;; Channels
(defn- channels
  "Returns a vector of channels representing drag events"
  [*state]
  (let [current-state @*state
        frames (get-in current-state [:config :frames])
        frame-documents (map dom/getFrameContentDocument frames)
        documents (concat [js/document] frame-documents)]
    [(release documents :release)
     (move documents :move #(get @*state :dragging))
     (begin documents :begin
       #(can-start?
          {:event %1
           :containers (get-in @*state [:config :containers])}))]))

;;;; Pointer channel
(defn- update-pointer-state
  "Updates the pointer state atom with relevant message data"
  [[message-name body] *pointer-state]
  (swap! *pointer-state assoc :name message-name))

(defn- post-release-move?
  "Determines if the move happened after a release. This addresses
  a known Chrome bug where a move event is fired after the mouse has
  been released"
  [message *pointer-state]
  (let [[message-name _] message
        previous-name (:name @*pointer-state)]
    (and
      (= :move message-name)
      (= :release previous-name))))

(defn pointer-chan
  "Returns a single channel that receives touch and mouse messages"
  [*state]
  (let [*pointer-state (atom {})
        event-channels (channels *state)
        out (chan)]
    (go-loop []
      (let [[message channel] (alts! event-channels)]
        (when-not (post-release-move? message *pointer-state)
          (>! out message))
        (update-pointer-state message *pointer-state))
      (recur))
    out))
