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
        screen-x (.-screenX event)
        screen-y (.-screenY event)
        client-x (.-clientX event)
        client-y (.-clientY event)
        point (Coordinate. screen-x screen-y)
        client (Coordinate. client-x client-y)]
    (hash-map :point point :target target :client client)))

(defmulti element-from-point (fn [element x y] (type element)))

(defmethod element-from-point
  js/HTMLIFrameElement
  [iframe x y]
  (let [rect (.getBoundingClientRect iframe)
        doc (dom/getFrameContentDocument iframe)
        left (.-left rect)
        top (.-top rect)]
    (.elementFromPoint doc
      (- x left)
      (- y top))))

(defmethod element-from-point :default
  [element _ _]
  element)

(defn move-message
  [event _]
  (let [msg (pointer-message event _)
        point (:client msg)
        x (.-x point)
        y (.-y point)
        target (:target msg)
        doc (.-ownerDocument target)
        element (.elementFromPoint doc x y)]
    (merge msg {:element (element-from-point element x y)})))

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

(defn is-leaving?
  "Check if the message represents leaving a drag container"
  [message prev current]
  (let [[message-name _] message
        different-elements? (and (= :move message-name) (not= current prev))]
    (if different-elements?
      (is-container? prev)
      false)))

;;;; Global State
(defonce pointer-state (atom {}))

(defn- update-pointer-state
  "Updates the pointer state atom with relevant message data"
  [[message-name body]]
  (let [{:keys [target element]} body]
    (swap! pointer-state merge
      {:name message-name :target target :element element})))

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
             {:keys [element]} body
             prev-state @pointer-state
             last-element (:element prev-state)
             leaving? (is-leaving? message last-element element)]
         (update-pointer-state message)
         (when-not (and (= :move message-name) (= :release (:name prev-state)))
           (if leaving?
             (>! out [:leave (assoc body :previous last-element)])
             (>! out message))))
       (recur))
     out))
  ([]
   (pointer-chan {})))
