(ns drago.core
  (:require [cljs.core.async :refer [<! chan pipe close!]]
            [drago.config :as config]
            [drago.pointer :as ptr]
            [drago.reduce :refer [reduce-state]]
            [drago.view :as view])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn- update-state
  "Update state based on the contents of a message"
  [prev-state [message-name body]]
  (let [msg {:message {:name message-name
                       :body body}}]
    (-> prev-state
        (merge msg)
        reduce-state)))

(defn- drain!
  "Helper for consuming all input on a channel before closing it.
  This is particularly helpful for shutting down pointer inputs since
  there could be a lot of pending messages on that channel"
  [ch]
  (go-loop []
    (if (some? (<! ch))
      (recur)
      (close! ch))))

;;;; Core drago API
(defrecord DragContext [in out pointer loop])

(defn stop! [ctx]
  "Closes all channels used in a drag context. @todo remove listeners mang"
  (let [{:keys [out pointer loop]} ctx]
    (close! loop)
    (close! out)
    (drain! pointer)))

(defn drago
  "Initialize the people's champion!"
  [drago-config]
  (let [config (config/create drago-config)
        start-state { :config config }
        pointer-chan (ptr/pointer-chan config)
        in (chan)
        out (chan)]
    (pipe pointer-chan in)
    (map->DragContext
      {:in in
       :out out
       :pointer pointer-chan
       :loop 
       (go-loop [prev-state start-state]
         (let [message (<! in)
               new-state (update-state prev-state message)]
           (view/render new-state prev-state)
           (recur new-state)))})))
