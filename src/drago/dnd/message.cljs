(ns drago.dnd.message
  (:require [goog.dom :as dom]
            [drago.message :as message]))

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

(defn drag
  "Extends the pointer message to include the element that the cursor
   is currently over"
  [event _]
  (let [msg (message/pointer event _)
        point (:client msg)
        x (.-x point)
        y (.-y point)
        target (:target msg)
        doc (.-ownerDocument target)
        element (.elementFromPoint doc x y)]
    (merge msg {:element (element-from-point element x y)})))

