(ns drago.dnd.core
  (:require [drago.dnd.config :as config]
            [drago.dnd.reduce :as reducer]
            [drago.context :as context]
            [drago.pointer :as pointer]))

(defn- replace!
  "Because threading is love. Threading is life"
  [val *atom]
  (reset! *atom val))

(defn- update-state
  "Update state based on the contents of a message"
  [*state [message-name body]]
  (let [msg {:message {:name message-name
                       :body body}}]
    (-> *state
        deref
        (merge msg)
        reducer/reduce
        (replace! *state))))

(defn start
  [configuration]
  (let [*state (atom {:config (config/create configuration)})
        pointer-channel (pointer/pointer-chan *state)]
    (context/create
      *state
      update-state
      pointer-channel)))
