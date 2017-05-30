(ns drago.dnd.core
  (:require [drago.dnd.config :as config]
            [drago.dnd.reduce :as reducer]
            [drago.context :as context]
            [drago.pointer :as pointer]))


(defn- update-state
  "Update state based on the contents of a message"
  [state [message-name body]]
  (-> state
      (assoc :message {:name message-name
                       :body body})
      reducer/reduce))


(defn start
  [configuration]
  (let [*state (atom {:config (config/create configuration)})
        pointer-channel (pointer/pointer-chan *state)]
    (context/create
      *state
      update-state
      pointer-channel)))
