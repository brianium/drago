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

(defonce doc (.-documentElement js/document))
(defonce iframe (dom/getElement "frame"))

(if iframe
  (defstate drago-config :start {:documents [doc (dom/getFrameContentDocument iframe)]})
  (defstate drago-config :start {}))

(defstate drag-loop :start (drago @drago-config)
  :stop (close! @drag-loop))

(defn teardown []
  (.log js/console "Teardown")
  (events/removeAll js/document "mousedown")
  (events/removeAll js/document "mouseup")
  (events/removeAll js/document "mousemove")
  (events/removeAll js/document "mouseover")
  (events/removeAll js/document "mouseenter")
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

