(ns drago.view
  (:require [goog.dom :as dom]
            [goog.dom.classlist :as classes]
            [goog.style :as style]
            [goog.style.transform :as transform]))

;;; Draw begin state
(defn- init-clone-position
  [{:keys [mirror rect]}]
  (style/setPosition
    mirror
    (.-left rect)
    (.-top rect)))

(defn- append-element
  [{:keys [document mirror rect]}]
  (style/setSize mirror (.-width rect) (.-height rect))
  (dom/appendChild (.-body document) mirror))

(defn- add-start-classes
  [{:keys [element]}]
  (classes/add element "drago-dragging"))

(def begin
  (juxt init-clone-position append-element add-start-classes))

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
  (when element
    (classes/remove element "drago-dragging")))

(def release
  (juxt remove-element remove-start-classes))

(defn render [{:keys [name] :as data}]
  (case name
    :begin (begin data)
    :move (position-element data)
    :release (release data)
    ""))
