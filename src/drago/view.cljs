(ns drago.view
  (:require [goog.dom :as dom]
            [goog.dom.classlist :as classes]
            [goog.style :as style]
            [goog.style.transform :as transform]))

;;; Draw begin state
(defn- init-clone-position
  [{:keys [mirror drag-source]} _]
  (let [rect (:rect drag-source)]
    (style/setPosition
      mirror
      (.-left rect)
      (.-top rect))))

(defn- append-element
  [{:keys [mirror drag-source]} _]
  (let [rect (:rect drag-source)
        document (:document drag-source)]
    (style/setSize mirror (.-width rect) (.-height rect))
    (dom/appendChild (.-body document) mirror)))

(defn- add-start-classes
  [{:keys [drag-source]} _]
  (classes/add
    (:element drag-source)
    "drago-dragging"))

(def begin
  (juxt init-clone-position append-element add-start-classes))

;;; Draw move state
(defn- position-element
  [{:keys [mirror drag-source]} _]
  (let [rect (:rect drag-source)]
    (when mirror
      (transform/setTranslation
        mirror
        (- (:x drag-source) (.-left rect))
        (- (:y drag-source) (.-top rect))))))

(defn- over-container
  [{:keys [drop-target dragging]} _]
  (let [container (:container drop-target)]
    (when (and dragging container)
      (classes/add container "drago-over"))))

(def move
  (juxt position-element over-container))

;;; Draw leave state
(defn leave
  [_ {{:keys [container]} :drop-target}]
  (when container
    (classes/remove container "drago-over")))

;;; Draw release state
(defn- remove-element
  [{:keys [mirror]} _]
  (dom/removeNode mirror))

(defn- remove-start-classes
  [{{:keys [element]} :drag-source} _]
  (when element
    (classes/remove element "drago-dragging")))

(defn- remove-container-classes
  [{{:keys [container]} :drop-target} _]
  (when container
    (classes/remove container "drago-over")))

(def release
  (juxt remove-element remove-start-classes leave))

(defn render
  [{{:keys [name]} :message :as state} prev-state]
  (case name
    :begin (begin state prev-state)
    :move (move state prev-state)
    :leave (leave state prev-state)
    :release (release state prev-state)
    ""))
