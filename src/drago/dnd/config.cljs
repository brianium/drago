(ns drago.dnd.config
  (:require [goog.dom :as dom]))

;;; The default drago configuration
(defonce default-config
  {:frames []
   :render true})

(defn- get-containers
  "Get drag containers from user config or create a default set"
  [{:keys [frames containers]}]
  (or
    containers
    (->>
      frames
      (map dom/getFrameContentDocument)
      (concat [js/document])
      (mapcat #(array-seq (dom/getElementsByClass "drago-container" %1))))))

(defn create
  "Merges a user supplied configuration into a set of drago defaults"
  ([config]
   (-> default-config
       (merge config)
       (assoc :containers (get-containers config))))

  ([]
   (create {})))
