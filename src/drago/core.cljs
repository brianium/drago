(ns drago.core
  (:require [goog.dom.classlist :as classes]
            [goog.dom :as dom]
            [goog.events :as events]
            [mount.core :as mount]
            [cljs.core.async :refer [chan put! <! close!]]
            [drago.pointer :as ptr]
            [drago.reduce :refer [reduce-state]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [mount.core :refer [defstate]])
  (:import goog.math.Coordinate))

(enable-console-print!)

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

(defn drago
  "Initialize the people's champion!"
  [start-state]
  (go-loop [state start-state]
    (draw state)
    (let [[name message] (<! @pointer-chan)
          {:keys [target document point]} message]
      (recur (reduce-state (merge state {:name name
                                         :target target
                                         :document document
                                         :point point}))))))

(defstate drago-loop :start (drago {})
              :stop (close! @drago-loop))

(defn teardown []
  (events/removeAll js/document "mousedown")
  (events/removeAll js/document "mouseup")
  (events/removeAll js/document "mousemove"))

(defn on-js-reload []
  (teardown)
  (mount/stop))

(mount/in-cljc-mode)
(mount/start)
