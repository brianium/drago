(ns drago.view
  (:require [goog.dom :as dom]))

(defn- append-element
  [{:keys [document element]}]
  (dom/appendChild (.-body document) element))

(defn- position-element
  [{:keys [element x y]}]
  (when element
    (set! (.. element -style -left) (str x "px"))
    (set! (.. element -style -top) (str y "px"))))

(defn- remove-element
  [{:keys [element]}]
  (dom/removeNode element))

(defn render [{:keys [name] :as data}]
  (case name
    :begin (append-element data)
    :move (position-element data)
    :release (remove-element data)
    ""))
