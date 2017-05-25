(ns drago.core
  (:require [drago.context :as context]
            [drago.dnd.core :as dnd]))

(defn stop!
  "Closes all channels used in a drag context.
  @todo remove event listeners"
  [ctx]
  (context/stop! ctx))

(defn dnd
  "Start draggin and droppin"
  [drago-config]
  (dnd/start drago-config))
