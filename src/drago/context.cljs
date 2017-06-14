(ns drago.context
  (:require [cljs.core.async :as async :refer [<! >! chan]]
            [drago.dnd.view :as view])
  (:require-macros [cljs.core.async.macros :refer [go-loop go]])
  (:refer-clojure :exclude [reduce]))


(defrecord DragContext [in pointer loop state])


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
  (let [{:keys [pointer loop]} ctx]
    (async/close! loop)
    (drain! pointer)))


(defn subscribe
  "Binds a function to a drag context. The function will be called
  with the new and previous drag state when state changes occur"
  ([ctx func watch-key]
   (-> ctx
     :state
     (add-watch watch-key
       (fn [_key _ref _old _new]
         (func _new _old))))
   ctx)
  ([ctx func]
   (subscribe ctx func (gensym "drago_"))))


(defn publish
  "Sends a message to the drag context. This message should be consumed
   a reducer"
  ([ctx message]
   (go
     (>! (:in ctx) message)))
  ([ctx message-name message-body]
   (publish ctx [message-name message-body])))


(defn- render-default?
  "Is drago doing any rendering?"
  [{:keys [config]}]
  (:render config))


(defn- with-message
  "Includes the message in state"
  [state [message-name body]]
  (assoc state :message {:name message-name :body body}))


(defn create
  "Creates a new DragContext. The DragContext contains all channels
  and event streams used for updating internal state for a drag operation"
  [*state reduce pointer]
  (let [in  (chan)]
    (async/pipe pointer in)
    (map->DragContext
      {:in in
       :pointer pointer
       :state *state
       :loop
       (go-loop []
         (let [prev-state          @*state
               message             (<! in)
               new-state           (reduce (with-message prev-state message))]
           (when (render-default? new-state)
             (view/render new-state prev-state))
           (reset! *state new-state)
           (recur)))})))
