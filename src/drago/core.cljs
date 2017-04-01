(ns drago.core
  (:require [cljs.core.async :refer [<!]]
            [drago.pointer :as ptr]
            [drago.reduce :refer [reduce-state]]
            [drago.view :refer [render]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn- update-state
  "Update state based on the contents of a message"
  [prev-state [message-name body]]
  (let [msg {:message {:name message-name
                       :body body}}]
    (-> prev-state
        (merge msg)
        reduce-state)))

(defn drago
  "Initialize the people's champion!"
  ([]
   (drago {} {}))
  
  ([config]
   (drago config {}))
  
  ([config start-state]
   (let [pointer-chan (ptr/pointer-chan config)]
     (go-loop [prev-state start-state]
       (let [message (<! pointer-chan)
             new-state (update-state prev-state message)]
         (render new-state prev-state)
         (recur new-state))))))

