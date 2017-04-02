(ns drago.message
  (:require [goog.dom :as dom])
  (:import goog.math.Coordinate))

;;; Defines a multi-method for using elementFromPoint with
;;; and without iframes
(defmulti element-from-point (fn [element x y] (type element)))

(defmethod element-from-point
  js/HTMLIFrameElement
  [iframe x y]
  (let [rect (.getBoundingClientRect iframe)
        doc (dom/getFrameContentDocument iframe)
        left (.-left rect)
        top (.-top rect)]
    (.elementFromPoint doc
      (- x left)
      (- y top))))

(defmethod element-from-point :default
  [element _ _]
  element)

(defn pointer-message
  "Creates a pointer message containing a coordinate point,
   the event target, and the event document"
  [event _]
  (let [target (.-target event)
        screen-x (.-screenX event)
        screen-y (.-screenY event)
        client-x (.-clientX event)
        client-y (.-clientY event)
        point (Coordinate. screen-x screen-y)
        client (Coordinate. client-x client-y)]
    (hash-map :point point :target target :client client)))

(defn move-message
  "Extends the pointer message to include the element that the cursor
   is currently over"
  [event _]
  (let [msg (pointer-message event _)
        point (:client msg)
        x (.-x point)
        y (.-y point)
        target (:target msg)
        doc (.-ownerDocument target)
        element (.elementFromPoint doc x y)]
    (merge msg {:element (element-from-point element x y)})))
