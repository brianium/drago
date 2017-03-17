(ns drago.core
  (:require [cljs.core.async :refer [<! alts!]]
            [drago.pointer :as ptr]
            [drago.frames :as frames]
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
   (let [pointer-chan (ptr/pointer-chan config)
         frame-chan (frames/frame-chan config)]
     (go-loop [state start-state]
       (render state)
       (let [[data channel] (alts! [pointer-chan frame-chan])
             message-name (first data)
             message (second data)]
         (recur (reduce-state (merge state {:name message-name
                                            :target (:target message)
                                            :point (:point message)}))))))))

