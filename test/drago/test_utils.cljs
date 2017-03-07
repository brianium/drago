(ns drago.test-utils
  (:require [goog.dom :as dom]
            [goog.dom.classlist :as classes]))

(defn append-element [element id class-name]
  (set! (.-id element) id)
  (classes/add element class-name)
  (dom/appendChild (.-body js/document) element))

(defn remove-element [id]
  (let [element (dom/getElement id)]
    (dom/removeNode element)))

(defn create-mouse-event
  "Create a MouseEvent to dispatch"
  [event-type]
  (let [event (.createEvent js/document "MouseEvent")]
    (.initMouseEvent event
      event-type
      true
      true
      js/window
      nil
      0 0 0 0
      false false false false
      0
      nil)
    event))

(defn mousedown
  "Dipatch a mouse down event to the given element"
  [el]
  (let [event (create-mouse-event "mousedown")]
    (.dispatchEvent el event)))

(defn mousemove
  "Dispatches a mousemove event to the element"
  [el]
  (let [event (create-mouse-event "mousemove")]
    (.dispatchEvent el event)))

(defn mouseup
  "Dispatches a mouseup event to the element"
  [el]
  (let [event (create-mouse-event "mouseup")]
    (.dispatchEvent el event)))
