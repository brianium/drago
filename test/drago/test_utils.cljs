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
  ([event-type bubbles button]
   (let [event (.createEvent js/document "MouseEvent")]
     (.initMouseEvent event
       event-type
       bubbles
       true
       js/window
       nil
       0 0 0 0
       false false false false
       button
       nil)
     event))
  ([event-type bubbles]
   (create-mouse-event event-type bubbles 0))
  ([event-type]
   (create-mouse-event event-type true 0)))

(defn mousedown
  "Dipatch a mouse down event to the given element"
  ([el bubbles button]
   (let [event (create-mouse-event "mousedown" bubbles button)]
     (.dispatchEvent el event)))
  ([el bubbles]
   (mousedown el bubbles 0))
  ([el]
   (mousedown el true)))

(defn mousemove
  "Dispatches a mousemove event to the element"
  ([el bubbles]
   (let [event (create-mouse-event "mousemove" bubbles)]
     (.dispatchEvent el event)))
  ([el]
   (mousemove el true)))

(defn mouseup
  "Dispatches a mouseup event to the element"
  [el]
  (let [event (create-mouse-event "mouseup")]
    (.dispatchEvent el event)))
