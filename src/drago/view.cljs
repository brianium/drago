(ns drago.view
  "Provides a minimal view layer for drag state.

  All view functions are given the current and previous state"
  (:require [goog.dom :as dom]
            [goog.dom.classlist :as classes]
            [goog.style :as style]
            [goog.style.transform :as transform]))

;;; Draw begin state
(defn- init-clone-position
  "Position the mirror element on top of the drag source"
  [{:keys [mirror drag-source]} _]
  (let [rect (:rect drag-source)]
    (style/setPosition
      mirror
      (.-left rect)
      (.-top rect))))

(defn- append-element
  "Fixes dimensions of the mirror to the drag source and appends
  the mirror element to the drag source's parent document"
  [{:keys [mirror drag-source]} _]
  (let [rect (:rect drag-source)
        document (:document drag-source)]
    (style/setSize mirror (.-width rect) (.-height rect))
    (dom/appendChild (.-body document) mirror)))

(defn- add-start-classes
  "Adds a class to the drag source to indicate it is being dragged"
  [{:keys [drag-source]} _]
  (classes/add
    (:element drag-source)
    "drago-dragging"))

;;; The main view function for handling the start of a drag
(def begin
  (juxt init-clone-position append-element add-start-classes))

;;; Draw move state
(defn- position-element
  "Translate the mirror element during a drag"
  [{:keys [mirror drag-source]} _]
  (let [rect (:rect drag-source)]
    (when mirror
      (transform/setTranslation
        mirror
        (- (:x drag-source) (.-left rect))
        (- (:y drag-source) (.-top rect))))))

(defn- over-container
  "Adds a class to the drag container that the pointer is currently over"
  [{:keys [drop-target dragging]} _]
  (let [container (:container drop-target)]
    (when (and dragging container)
      (classes/add container "drago-over"))))

(defn- leave
  "Removes relevant classes from a drop container that is no longer
  in focus"
  [state prev-state]
  (let [current (get-in state [:drop-target :container])
        prev (get-in prev-state [:drop-target :container])]
    (when-not (= current prev)
      (when prev
        (classes/remove prev "drago-over")))))

;;; The main view function for handling drag movement
(def move
  (juxt position-element over-container leave))

;;; Draw release state
(defn- remove-element
  "Remove the mirror element from the dom"
  [{:keys [mirror]} _]
  (dom/removeNode mirror))

(defn- remove-start-classes
  "Remove classes from the drag source"
  [{{:keys [element]} :drag-source} _]
  (when element
    (classes/remove element "drago-dragging")))

(defn- remove-container-classes
  "Remove classes from the drag container"
  [{{:keys [container]} :drop-target} _]
  (when container
    (classes/remove container "drago-over")))

;;; The main view function for handling a drag release
(def release
  (juxt remove-element remove-start-classes remove-container-classes))

(defn render
  "Renders the current state of a drag operation"
  [{{:keys [name]} :message :as state} prev-state]
  (case name
    :begin (begin state prev-state)
    :move (move state prev-state)
    :release (release state prev-state)
    ""))
