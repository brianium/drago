(ns drago.core
  (:require [cljs.core.async :refer [<!]]
            [drago.config :as config]
            [drago.pointer :as ptr]
            [drago.reduce :refer [reduce-state]]
            [drago.view :as view])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn- update-state
  "Update state based on the contents of a message"
  [prev-state [message-name body] config]
  (let [msg {:message {:name message-name
                       :body body}}]
    (-> prev-state
        (merge msg)
        (reduce-state config))))

(defn drago
  "Initialize the people's champion!"
  [drago-config]
  (let [config (config/create drago-config)
        pointer-chan (ptr/pointer-chan config)
        {:keys [start-state render]} config]
    (go-loop [prev-state start-state]
      (let [message (<! pointer-chan)
            new-state (update-state prev-state message config)]
        (view/render new-state prev-state)
        (render new-state prev-state)
        (recur new-state)))))
