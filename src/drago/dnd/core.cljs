(ns drago.dnd.core
  (:require [drago.dnd.config :as config]
            [drago.dnd.reduce :as reducer]
            [drago.context :as context]
            [drago.pointer :as pointer]))


;;;; Drag And Drop Subscriptions
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
  ([ctx func watch-key]
   (context/subscribe
     ctx
     (fn [state prev-state]
       (when (is-drop? state)
         (func
           state
           prev-state)))
     watch-key))
  ([ctx func]
   (on-drop ctx func (gensym "drago_on_drop_"))))


;;;; Drag and Drop Publishers
(defn add-container!
  [context container]
  (context/publish context :container {:container container}))


(defn start
  "Creates a DragContext for use in drag and drop operations"
  ([configuration reduce-fn]
   (let [*state (atom {:config (config/create configuration)})
         pointer-channel (pointer/pointer-chan *state)]
     (context/create
       *state
       (comp reduce-fn reducer/reduce)
       pointer-channel)))
  ([configuration]
   (start configuration identity))
  ([]
   (start {})))
