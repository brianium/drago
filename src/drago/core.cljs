(ns drago.core
  (:require [goog.dom.classlist :as classes]
            [goog.dom :as dom]
            [cljs.core.async :refer [<!]]
            [drago.pointer :as ptr]
            [drago.reduce :refer [reduce-state]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn clone-node [elem]
  (.cloneNode elem true))

(defn position-clone
  [clone rect]  
  (set! (.. clone -style -left) (str (.-left rect) "px"))
  (set! (.. clone -style -top) (str (.-top rect) "px")))

(defn draw-start
  [{:keys [target document rect]}]
  (let [clone (clone-node target)]
    (classes/add clone "mirror")
    (position-clone clone rect)
    (dom/appendChild (.-body document) clone)))

(defn draw-drag
  [{:keys [target x y]}]
  (set! (.. target -style -left) (str x "px"))
  (set! (.. target -style -top) (str y "px")))

(defn draw-end
  [{:keys [document]}]
  (let [mirror (.querySelector document ".mirror")]
    (dom/removeNode mirror)))

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

