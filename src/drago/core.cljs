(ns drago.core
  (:require [goog.dom.classlist :as classes]
            [goog.dom :as dom]
            [cljs.core.async :refer [<!]]
            [drago.pointer :as ptr]
            [drago.reduce :refer [reduce-state]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn draw-start
  [{:keys [document element]}]
  (dom/appendChild (.-body document) element))

(defn draw-drag
  [{:keys [element x y]}]
  (when element
    (set! (.. element -style -left) (str x "px"))
    (set! (.. element -style -top) (str y "px"))))

(defn draw-end
  [{:keys [element]}]
  (dom/removeNode element))

(defn draw [{:keys [name] :as data}]
  (case name
    :begin (draw-start data)
    :move (draw-drag data)
    :release (draw-end data)
    ""))

(defn drago
  "Initialize the people's champion!"
  [start-state]
  (let [pointer-chan (ptr/pointer-chan)]
    (go-loop [state start-state]
      (draw state)
      (let [[name message] (<! pointer-chan)]
        (recur (reduce-state (merge state {:name name
                                           :target (:target message)
                                           :document (:document message)
                                           :point (:point message)})))))))

