(ns drago.core
  (:require [drago.context :as context]
            [drago.dnd.core :as dnd]))


(defn stop!
  "Closes all channels used in a drag context.
  @todo remove event listeners"
  [ctx]
  (context/stop! ctx))


(defn subscribe
  "Binds a function to a drag context. The function will be called
  with the new and previous state when state changes occur"
  ([ctx func watch-key]
   (context/subscribe ctx func watch-key))
  ([ctx func]
   (context/subscribe ctx func)))


(defn publish
  "Sends a message to the drag context. This message should be consumed
  by a reducer"
  ([ctx message]
   (context/publish ctx message))
  ([ctx message-name message-body]
   (context/publish ctx message-name message-body)))


(defn dnd
  "Start draggin and droppin"
  ([drago-config reduce-fn]
   (dnd/start drago-config reduce-fn))
  ([drago-config]
   (dnd/start drago-config))
  ([]
   (dnd/start)))
