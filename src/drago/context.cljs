(ns drago.context
  (:require [cljs.core.async :as async :refer [<! >! chan]]
            [drago.dnd.view :as view])
  (:require-macros [cljs.core.async.macros :refer [go-loop go]])
  (:refer-clojure :exclude [reduce]))

(defrecord DragContext [in out pointer loop])

(defn- drain!
  "Helper for consuming all input on a channel before closing it.
  This is particularly helpful for shutting down pointer inputs since
  there could many pending messages on that channel"
  [ch]
  (go-loop []
    (if (some? (<! ch))
      (recur)
      (async/close! ch))))

(defn stop!
  "Closes all channels used in a drag context.
  @todo remove event listeners"
  [ctx]
  (let [{:keys [out pointer loop]} ctx]
    (async/close! loop)
    (async/close! out)
    (drain! pointer)))

(defn subscribe
  "Binds a function to a drag context. The function will be called
  with the new and previous drag state when state changes occur"
  [ctx func]
  (go-loop []
    (let [[new-state prev-state] (<! (:out ctx))]
      (func new-state prev-state)
      (recur)))
  ctx)

(defn publish
  "Sends a message to the drag context. This message should be consumed
   a reducer"
  ([ctx message]
   (go
     (>! (:in ctx) message)))
  ([ctx message-name message-body]
   (publish ctx [message-name message-body])))

(defn create
  [state reduce pointer]
  (let [in (chan)
        out (chan (async/sliding-buffer 10))]
    (async/pipe pointer in)
    (map->DragContext
      {:in in
       :out out
       :pointer pointer
       :loop
       (go-loop []
         (let [prev-state @state
               message (<! in)
               new-state (reduce state message)]
           (view/render new-state prev-state)
           (async/put! out [new-state prev-state])
           (recur)))})))
