(ns drago.dev
  (:require [mount.core :as mount]
            [cljs.core.async :refer [close!]]
            [goog.events :as events]
            [goog.dom :as dom]
            [drago.core :refer [drago]]
            [drago.pointer :as ptr])
  (:require-macros [mount.core :refer [defstate]]))

(enable-console-print!)

(mount/in-cljc-mode)

;; develop against a cross frame environment
(defstate drago-config :start {:move-targets [(.-documentElement js/document)
                                              (.. (dom/getElement "frame") -contentWindow -document)]})

(defstate drag-loop :start (drago @drago-config)
  :stop (close! @drag-loop))

(defn teardown []
  (.log js/console "Teardown")
  (events/removeAll js/document "mousedown")
  (events/removeAll js/document "mouseup")
  (events/removeAll js/document "mousemove")
  (mount/stop))

(defn setup []
  (.log js/console "Setup")
  (reset! ptr/pointer-state {})
  (mount/start))

(defn on-js-reload []
  (.log js/console "reloading")
  (teardown)
  (setup))

(mount/start)

