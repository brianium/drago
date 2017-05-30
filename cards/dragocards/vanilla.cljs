(ns dragocards.vanilla
  "This set of devcards demonstrates dragos API using vanilla dom operations.
  No react/virtual doms here."
  (:require [devcards.core]
            [sablono.core :as sab]
            [goog.dom :as dom]
            [goog.dom.classlist :as classes]
            [drago.core :as drago]
            [drago.dnd.core :as dnd])
  (:require-macros [devcards.core :refer [defcard defcard-doc dom-node]]))

(defn html [str]
  (dom/constHtmlToNode (.from goog.string/Const str)))


(defn with-clean [func]
  (fn [_ node]
    (set! (.-innerHTML node) "")
    (func node)))


(defn pointer-detection []
  (dom-node
    (with-clean
      (fn [node]
        (let [left (html "<div class=\"drago-container card-container\">
                            <div class=\"rectangle\"></div>
                          </div>")
              right (html "<div class=\"drago-container card-container\">
                             <div class=\"rectangle\"></div>
                           </div>")
              ch (drago/dnd {:containers [left right]})]
          (classes/add node "drag-demo")
          (dom/append node left right))))))


(defcard-doc
  "## Drago With The Vanilla DOM


  While drago is an excellent library to use with re-frame and the like -
  it is also useful when working with plain'ol native DOM apis.

  The following examples demonstrate some basic usage of drago within the vanilla
  DOM")


(defcard
  "## Basic pointer detection

  Lets start by demonstrating drago's basic pointer detection

  Without configuration, drago will register any element with a class
  of drago-container as an element capable of supporting drag.

  The default behavior of drago is to clone draggable elements and simply
  add classes to represent each state.

  In the following example `left` and `right` are dom elements.

  ```clojure
  (drago/dnd {:containers [left right]})
  ```"
  (pointer-detection))


(defn nested-containers []
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
          (drago/dnd {:containers [(dom/getElement "parent")
                                   (dom/getElement "parent2")
                                   (dom/getElement "nested")
                                   (dom/getElement "nested2")]}))))))


(defcard
  "## Nested containers
  
  We can even nest containers - making containers themselves draggable.

  ```clojure
  (drago/dnd {:containers [parent nested parent2 nested2]})
  ```"
  (nested-containers))


(defn handle-drop
  [state prev-state]
  (let [container (get-in state [:drop-target :container])
        element   (get-in prev-state [:drag-source :element])]
    (dom/append
      container
      (.cloneNode element true))))


(defn toolbox [handler]
  (dom-node
    (with-clean
      (fn [node]
        (let [fragment
              (html "<div class=\"drag-demo\">
                       <ul id=\"toolbox\" class=\"drago-container card-container\">
                         <li>tool 1</li>
                         <li>tool 2</li>
                         <li>tool 3</li>
                       </ul>
                       <ul id=\"dropzone\" class=\"drago-container card-container\">
                       </ul>
                     </div>")]
          (dom/append node fragment)
          (dnd/on-drop
            (drago/dnd {:containers [(dom/getElement "toolbox")
                                     (dom/getElement "dropzone")]})
            handler))))))


(defcard
  "## Toolbox Example

  We can bind functions to state changes using the subscribe function. Any function
  bound this way receives the new and previous state. This can be used for additional
  rendering, or side effects.

  We can create a simple toolbox ui by listening with a function.

  ```clojure
  (defn handle-drop
    [state prev-state]
    (let [container (get-in state [:drop-target :container])
          element   (get-in prev-state [:drag-source :element])]
    (dom/append
      container
      (.cloneNode element true))))

  ;; create a drag context and listen for state changes
  (-> (drago/dnd {:containers [toolbox dropzone]})
      (dnd/on-drop handle-drop))
  ```"
  (toolbox handle-drop))
