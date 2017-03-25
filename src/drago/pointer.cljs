(ns drago.pointer
  (:require [cljs.core.async :refer [chan >! alts!]]
            [goog.dom :as dom]
            [goog.dom.classlist :as classes]
            [goog.array :refer [contains]]
            [drago.streams :refer [stream-factory]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:import goog.math.Coordinate
           goog.events.BrowserEvent))

(defn pointer-message
  "Creates a pointer message containing a coordinate point,
   the event target, and the event document"
  [event _]
  (let [target (.-target event)
        x (.-screenX event)
        y (.-screenY event)
        point (Coordinate. x y)]
    (hash-map :point point :target target)))

;;;; Pointer Streams
(def begin
  (stream-factory (array "mousedown" "touchstart") pointer-message))

(def release
  (stream-factory (array "mouseup" "touchend" "touchcancel") pointer-message))

(def move
  (stream-factory (array "mousemove" "touchmove") pointer-message))

;;;; Stream Filters
(defn- is-left-click-or-touch?
  "Detect if the event is a left click or a touch"
  [event]
  (or (= "touchstart" (.-type event))
    (.isButton event (.. BrowserEvent -MouseButton -LEFT))))

(defn- is-container?
  [container]
  (if container
    (classes/contains container "drago-container")
    false))

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

(defn- update-pointer-state
  "Updates the pointer state atom with relevant message data"
  [[message-name body]]
  (let [{:keys [target]} body]
    (swap! pointer-state merge
      {:name message-name :target target})))

(defn is-leaving?
  [message prev current]
  (let [[message-name body] message
        different-elements? (and (= :move message-name) (not= current prev))]
    (if different-elements?
      (is-container? prev)
      false)))

(defn pointer-chan
  "Returns a single channel that receives touch and mouse messages"
  ([config]
   (let [event-channels (channels config)
         out (chan)]
     (go-loop []
       (let [[message channel] (alts! event-channels)
             [message-name body] message
             {:keys [target]} body
             prev-state @pointer-state
             leaving? (is-leaving? message (:target prev-state) target)]
         (update-pointer-state message)
         (when-not (and (= :move message-name) (= :release (:name prev-state)))
           (if leaving?
             (>! out [:leave (assoc body :previous (:target prev-state))])
             (>! out message))))
       (recur))
     out))
  ([]
   (pointer-chan {})))
