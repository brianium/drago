(ns drago.core
  (:require [cljs.core.async :refer [<!]]
            [goog.dom :as dom]
            [drago.pointer :as ptr]
            [drago.reduce :refer [reduce-state]]
            [drago.view :as view])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

;;; The default drago configuration
(defonce default-config
  {:render identity
   :transform identity
   :start-state {}
   :frames []})

(defn- update-state
  "Update state based on the contents of a message"
  [prev-state [message-name body] config]
  (let [msg {:message {:name message-name
                       :body body}}]
    (-> prev-state
        (merge msg)
        (reduce-state config))))

(defn- get-containers
  "Get drag containers from user config or create a default set"
  [{:keys [frames containers]}]
  (if containers
    containers
    (->> frames
         (map dom/getFrameContentDocument)
         (concat [js/document])
         (mapcat #(array-seq (dom/getElementsByClass "drago-container" %1))))))

(defn- create-config
  "Merges a user supplied configuration into a set of drago defaults"
  [config]
  (-> default-config
      (merge config)
      (assoc :containers (get-containers config))))

(defn drago
  "Initialize the people's champion!"
  [drago-config]
  (let [config (create-config drago-config)
        pointer-chan (ptr/pointer-chan config)
        {:keys [start-state render]} config]
    (go-loop [prev-state start-state]
      (let [message (<! pointer-chan)
            new-state (update-state prev-state message config)]
        (view/render new-state prev-state)
        (render new-state prev-state)
        (recur new-state)))))
