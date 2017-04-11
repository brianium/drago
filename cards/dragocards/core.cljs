(ns dragocards.core
  (:require [devcards.core]
            [sablono.core :as sab]
            [drago.core :refer [drago]])
  (:require-macros [devcards.core :refer [defcard]]))

;;; Handles default drago behavior for all cards
(defonce default (drago))

(defn pointer-detection
  []
  (sab/html
    [:div.drag-demo
     [:div.drago-container.card-container
      [:div.rectangle]]
     [:div.drago-container.card-container
      [:div.rectangle]]]))

(defcard
  "## Basic pointer detection

  Lets start by demonstrating drago's basic pointer detection

  Without configuration, drago will register any element with a class
  of drago-container as an element capable of supporting drag.

  The default behavior of drago is to clone draggable elements and simply
  add classes to represent each state.

  ```
  (drago)
  ```"
  (pointer-detection))

(defn nested-containers
  []
  (sab/html
    [:div.drag-demo
     [:div.drago-container.card-container
      [:div.rectangle]
      [:div.drago-container.card-container.is-rectangle]]
     [:div.drago-container.card-container
      [:div.rectangle]
      [:div.drago-container.card-container.is-rectangle]]]))

(defcard
  "## Nested containers
  
  We can even nest containers - making containers themselves draggable.

  ```
  (drago)
  ```"
  (nested-containers))
