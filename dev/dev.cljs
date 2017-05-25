(ns drago.dev
  (:require [mount.core :as mount]
            [goog.events :as events]
            [goog.dom :as dom]
            [drago.core :as drago])
  (:require-macros [mount.core :refer [defstate]]))

(enable-console-print!)

;;; helper functions
(defn logged
  "Instruments a function to log its result before returning it"
  [func]
  (fn [& args]
    (let [result (apply func args)]
      (.log js/console (clj->js result))
      result)))

;;; set up dev environment - i.e the live example
(mount/in-cljc-mode)
(defonce doc (.-documentElement js/document))
(defonce iframe (dom/getElement "frame"))

;;; define configuration based on whether the script is running in an iframe or not
(defstate drago-config :start {:frames [iframe]})

;;; uses identity for a noop render method during development
(defstate drag-context :start (drago/start @drago-config)
                       :stop (drago/stop! @drag-context))

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
  (mount/start))

(defn on-js-reload []
  (.log js/console "reloading")
  (teardown)
  (setup))

(mount/start)

