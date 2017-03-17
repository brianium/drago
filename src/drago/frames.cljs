(ns drago.frames
  (:require [cljs.core.async :refer [chan >! <!]]
            [goog.dom :as dom]
            [goog.dom.classlist :as classes]
            [goog.style :as style]
            [drago.streams :refer [stream-factory]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:import goog.math.Coordinate))

(defrecord FrameMessage [point target])

(defn frame-message
  [event _]
  (let [target (.-target event)
        x (.-screenX event)
        y (.-screenY event)
        coords (Coordinate. x y)]
    (->FrameMessage coords target)))

;;; Frame Streams
(def move
  (stream-factory (array "mousemove" "touchmove") frame-message))

(defn frame-chan
  ([{:keys [frames]}]
   (let [documents (map dom/getFrameContentDocument frames)
         move-chan (move documents :frame-move)
         out (chan)]
     (go-loop []
       (let [data (<! move-chan)]
         (>! out data)
         (recur)))
     out)))
