(ns drago.core
  (:require [cljs.core.async :refer [<!]]
            [drago.pointer :as ptr]
            [drago.reduce :refer [reduce-state]]
            [drago.view :refer [render]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn drago
  "Initialize the people's champion!"
  ([]
   (drago {} {}))
  
  ([config]
   (drago config {}))
  
  ([config start-state]
   (let [pointer-chan (ptr/pointer-chan config)]
     (go-loop [state start-state]
       (render state)
       (let [[name message] (<! pointer-chan)]
         (when (= name :over)
           (.log js/console (str name)))
         (recur (reduce-state (merge state {:name name
                                            :target (:target message)
                                            :document (:document message)
                                            :point (:point message)}))))))))

