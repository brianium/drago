(ns drago.core
  (:require [cljs.core.async :refer [<! >! chan pipe close! put! sliding-buffer]]
            [drago.dnd.config :as config]
            [drago.context :as context]
            [drago.pointer :as ptr]
            [drago.reduce :refer [reduce-state]]
            [drago.dnd.view :as view])
  (:refer-clojure :exclude [reduce])
  (:require-macros [cljs.core.async.macros :refer [go-loop go]]))

(defn- replace!
  "Because threading is love. Threading is life"
  [val atom]
  (reset! atom val))

(defn- update-state
  "Update state based on the contents of a message"
  [state [message-name body]]
  (let [msg {:message {:name message-name
                       :body body}}]
    (-> state
        deref
        (merge msg)
        reduce-state
        (replace! state))))

(defn stop!
  "Closes all channels used in a drag context.
  @todo remove event listeners"
  [ctx]
  (context/stop! ctx))

(defn start
  [drago-config]
  (let [state (atom {:config (config/create drago-config)})]
    (context/create
      state
      update-state
      (ptr/pointer-chan state))))
