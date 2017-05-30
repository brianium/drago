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


(defn- is-drop?
  "Based on the current state, is this a drop operation"
  [state]
  (let [name        (get-in state [:message :name])
        drop-target (get state :drop-target)]
    (and
      (= :release name)
      (:container drop-target))))


(defn on-drop
  "Subscribes to a drop operation. The provided function will be called
  with the container, the element dragged and the current and previous
  drag states"
  [ctx func]
  (context/subscribe
    ctx
    (fn [state prev-state]
      (when (is-drop? state)
        (func
          state
          prev-state)))))


(defn start
  [configuration]
  (let [*state (atom {:config (config/create configuration)})
        pointer-channel (pointer/pointer-chan *state)]
    (context/create
      *state
      update-state
      pointer-channel)))
