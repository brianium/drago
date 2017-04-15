(ns dragocards.core
  (:require [devcards.core]
            [sablono.core :as sab]
            [goog.dom :as dom]
            [goog.dom.classlist :as classes]
            [drago.core :refer [drago]])
  (:require-macros [devcards.core :refer [defcard dom-node]]))

(defn html
  [str]
  (->
    (.from goog.string/Const str)
    dom/constHtmlToNode))

(defn with-clean
  [func]
  (fn [_ node]
    (set! (.-innerHTML node) "")
    (func node)))

(defn pointer-detection
  []
  (dom-node
    (with-clean
      (fn [node]
        (let [left (html "<div class=\"drago-container card-container\">
                            <div class=\"rectangle\"></div>
                          </div>")
              right (html  "<div class=\"drago-container card-container\">
                              <div class=\"rectangle\"></div>
                            </div>")
              ch (drago {:containers [left right]})]
          (classes/add node "drag-demo")
          (dom/append node left right))))))

(defcard
  "## Basic pointer detection

  Lets start by demonstrating drago's basic pointer detection

  Without configuration, drago will register any element with a class
  of drago-container as an element capable of supporting drag.

  The default behavior of drago is to clone draggable elements and simply
  add classes to represent each state.

  ```
  (drago {:containers [left right]})
  ```"
  (pointer-detection))

(defn nested-containers
  []
  (dom-node
    (with-clean
      (fn [node]
        (let [fragment (html
                         "<div class=\"drag-demo\">
                            <div class=\"drago-container card-container\" id=\"parent\">
                              <div class=\"rectangle\"></div>
                              <div class=\"drago-container card-container is-rectangle\" id=\"nested\"></div>
                            </div>
                            <div class=\"drago-container card-container\" id=\"parent2\">
                              <div class=\"rectangle\"></div>
                              <div class=\"drago-container card-container is-rectangle\" id=\"nested2\">
                              </div> 
                            </div>
                          </div>")]
          (dom/append node fragment)
          (drago {:containers [(dom/getElement "parent")
                               (dom/getElement "parent2")
                               (dom/getElement "nested")
                               (dom/getElement "nested2")]}))))))

(defcard
  "## Nested containers
  
  We can even nest containers - making containers themselves draggable.

  ```
  (drago {:containers [parent nested parent2 nested2]})
  ```"
  (nested-containers))
