(ns drago.pointer
  (:require [cljs.core.async :as async :refer [chan >!]]
            [goog.dom :as dom]
            [goog.dom.classlist :as classes]
            [drago.streams :as streams]
            [drago.message :as message]
            [drago.dnd.message :as dnd-message]
            [drago.dnd.container :as container])
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:import goog.events.BrowserEvent))


;;;; Pointer Streams
(def begin
  (streams/factory (array "mousedown" "touchstart") message/pointer))


(def release
  (streams/factory (array "mouseup" "touchend" "touchcancel") message/pointer))


(def move
  (streams/factory (array "mousemove" "touchmove") dnd-message/drag))


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
    (container/belongs-to-container?
      containers
      target)))


(def can-start? (every-pred
                  is-left-click-or-touch?
                  in-container?))


;;;; Channels
(defn- channels
  "Returns a vector of channels representing drag events"
  [*state]
  (let [current-state   @*state
        frames          (get-in current-state [:config :frames])
        frame-documents (map dom/getFrameContentDocument frames)
        documents       (concat [js/document] frame-documents)]
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
        previous-name    (:name @*pointer-state)]
    (and
      (= :move message-name)
      (= :release previous-name))))


(defn pointer-chan
  "Returns a single channel that receives touch and mouse messages"
  [*state]
  (let [*pointer-state (atom {})
        event-channels (channels *state)
        out            (chan)]
    (go-loop []
      (let [[message channel] (alts! event-channels)]
        (when-not (post-release-move? message *pointer-state)
          (>! out message))
        (update-pointer-state message *pointer-state))
      (recur))
    out))
