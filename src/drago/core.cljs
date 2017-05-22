(ns drago.core
  (:require [cljs.core.async :refer [<! >! chan pipe close! put! sliding-buffer]]
            [drago.dnd.config :as config]
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

(defn- drain!
  "Helper for consuming all input on a channel before closing it.
  This is particularly helpful for shutting down pointer inputs since
  there could many pending messages on that channel"
  [ch]
  (go-loop []
    (if (some? (<! ch))
      (recur)
      (close! ch))))

;;;; Core drago API
(defrecord DragContext [in out pointer loop])

(defn stop!
  "Closes all channels used in a drag context.
  @todo remove event listeners"
  [ctx]
  (let [{:keys [out pointer loop]} ctx]
    (close! loop)
    (close! out)
    (drain! pointer)))

(defn subscribe
  "Binds a function to a drag context. The function will be called
  with the new and previous drag state when state changes occur"
  [ctx func]
  (go-loop []
    (let [[new-state prev-state] (<! (:out ctx))]
      (func new-state prev-state)
      (recur))))

(defn publish
  "Sends a message to the drag context. This message should be consumed
   a reducer"
  ([ctx message]
   (go
     (>! (:in ctx) message)))
  ([ctx message-name message-body]
   (publish ctx [message-name message-body])))

(defn start
  "Initialize the people's champion!"
  ([drago-config reduce]
   (let [config (config/create drago-config)
         state (atom { :config config })
         pointer-chan (ptr/pointer-chan state)
         in (chan)
         out (chan (sliding-buffer 10))]
     (pipe pointer-chan in)
     (map->DragContext
       {:in in
        :out out
        :pointer pointer-chan
        :loop 
        (go-loop []
          (let [prev-state @state
                message (<! in)
                new-state (reduce state message)]
            (view/render new-state prev-state)
            (put! out [new-state prev-state])
            (recur)))})))
  ([drago-config]
   (start drago-config update-state)))
