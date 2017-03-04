(ns drago.core
  (:require [goog.dom.classlist :as classes]
            [goog.dom :as dom]
            [goog.events :as events]
            [mount.core :as mount]
            [cljs.core.async :refer [chan put! <! close!]]
            [drago.pointer :as ptr])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [mount.core :refer [defstate]])
  (:import goog.math.Coordinate))

(enable-console-print!)

(defn receive-press
  [[_ {:keys [target point]}] state]
  (let [rect (.getBoundingClientRect target)]
    (-> state
      (assoc :pressed true)
      (assoc :rect rect)
      (assoc :offset (Coordinate. (- (.-x point) (.-left rect))
                                  (- (.-y point) (.-top rect)))))))

(defn receive-move
  [_ {:keys [point offset] :as state}]
  (-> state
      (assoc :dragging true)
      (assoc :x (- (.-x point) (.-x offset)))
      (assoc :y (- (.-y point) (.-y offset)))))

(defn receive-release
  [_ state]
  (assoc state :pressed false))

(defn receive-message [data state]
  (let [message-name (first data)
        {:keys [target document point]} (second data)
        new-state (merge state {:name message-name
                                :target target
                                :document document
                                :point point})]
    (condp = message-name
      :begin (receive-press data new-state)
      :move (receive-move data new-state)
      :release (receive-release data new-state)
      new-state)))

;;; MAIN APP LOOP - MAINLY DEV
(defstate pointer-chan :start (ptr/pointer-chan)
                       :stop (close! @pointer-chan))

(defn clone-node [elem]
  (.cloneNode elem true))

(defn position-clone
  [clone rect]  
  (set! (.. clone -style -left) (str (.-left rect) "px"))
  (set! (.. clone -style -top) (str (.-top rect) "px")))

(defn draw-start
  [{:keys [target document rect]}]
  (let [clone (clone-node target)]
    (classes/add clone "mirror")
    (position-clone clone rect)
    (dom/appendChild (.-body document) clone)))

(defn draw-drag
  [{:keys [target x y]}]
  (set! (.. target -style -left) (str x "px"))
  (set! (.. target -style -top) (str y "px")))

(defn draw-end
  [{:keys [document]}]
  (let [mirror (.querySelector document ".mirror")]
    (dom/removeNode mirror)))

(defn draw [{:keys [name] :as data}]
  (case name
    :begin (draw-start data)
    :move (draw-drag data)
    :release (draw-end data)
    ""))

(defn app-loop [start-state]
  (go-loop [state start-state]
    (draw state)
    (recur (receive-message (<! @pointer-chan) state))))

(defstate app :start (app-loop {})
              :stop (close! @app))

(defn teardown []
  (events/removeAll js/document "mousedown")
  (events/removeAll js/document "mouseup")
  (events/removeAll js/document "mousemove"))

(defn on-js-reload []
  (teardown)
  (mount/stop))

(mount/in-cljc-mode)
(mount/start)
