(ns drago.view
  (:require [goog.dom :as dom]
            [goog.dom.classlist :as classes]
            [goog.style.transform :as transform]))

;;; Draw begin state
(defn- append-element
  [{:keys [document mirror]}]
  (dom/appendChild (.-body document) mirror))

(defn- add-start-classes
  [{:keys [element]}]
  (classes/add element "drago-dragging"))

(def begin
  (juxt append-element add-start-classes))

;;; Draw move state
(defn- position-element
  [{:keys [mirror x y rect]}]
  (when mirror
    (transform/setTranslation
      mirror
      (- x (.-left rect))
      (- y (.-top rect)))))

;;; Draw release state
(defn- remove-element
  [{:keys [mirror]}]
  (dom/removeNode mirror))

(defn- remove-start-classes
  [{:keys [element]}]
  (classes/remove element "drago-dragging"))

(def release
  (juxt remove-element remove-start-classes))

(defn render [{:keys [name] :as data}]
  (case name
    :begin (begin data)
    :move (position-element data)
    :release (release data)
    ""))
