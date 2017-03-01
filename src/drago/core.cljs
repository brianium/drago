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

;;; DOM FUNCTIONS
(defn clone-node [elem]
  (.cloneNode elem true))

(defn position-clone
  [target clone]
  (let [rect (.getBoundingClientRect target)]
    (set! (.. clone -style -left) (str (.-left rect) "px"))
    (set! (.. clone -style -top) (str (.-top rect) "px"))))

(defn receive-press [[name {:keys [point target document]}]]
  (let [clone (clone-node target)]
    (.log js/console name)
    (classes/add clone "mirror")
    (dom/appendChild (.-body document) clone)
    (position-clone target clone)))

(defn receive-move [[name {:keys [point]}]]
  (.log js/console name point))

(defn receive-release [[name release]]
  (let [mirror (dom/getElementByClass "mirror")]
    (.log js/console name)
    (dom/removeNode mirror)))

(defn receive-message [data]
  (let [name (first data)]
    (when (= name "begin")
      (receive-press data))
    (when (= name "move")
      (receive-move data))
    (when (= name "release")
      (receive-release data))))

;;; MAIN APP LOOP - MAINLY DEV
(defstate pointer-chan :start (ptr/pointer-chan)
                     :stop (close! @pointer-chan))

(defn app-loop []
  [(go-loop []
     (when-let [message (<! @pointer-chan)]
       (receive-message message))
     (recur))])

(defstate app :start (app-loop)
              :stop (doseq [ch @app]
                      (close! ch)))

(defn teardown []
  (events/removeAll js/document "mousedown")
  (events/removeAll js/document "mouseup")
  (events/removeAll js/document "mousemove"))

(defn on-js-reload []
  (teardown)
  (mount/stop))

(mount/in-cljc-mode)
(mount/start)
