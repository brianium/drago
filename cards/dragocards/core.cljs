(ns dragocards.core
  (:require [devcards.core]
            [sablono.core :as sab]
            [goog.dom :as dom]
            [goog.dom.classlist :as classes]
            [drago.core :refer [drago receive]])
  (:require-macros [devcards.core :refer [defcard dom-node]]))

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

  ```clojure
  (drago {:containers [left right]})
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
          (drago {:containers [(dom/getElement "parent")
                               (dom/getElement "parent2")
                               (dom/getElement "nested")
                               (dom/getElement "nested2")]}))))))

(defcard
  "## Nested containers
  
  We can even nest containers - making containers themselves draggable.

  ```clojure
  (drago {:containers [parent nested parent2 nested2]})
  ```"
  (nested-containers))

(defn handle-drop [state _]
  (let [name (get-in state [:message :name])
        drop-target (get state :drop-target)
        drag-source (get state :drag-source)]
    (when (and (= :release name) (:container drop-target))
      (dom/append
        (:container drop-target)
        (.cloneNode (:element drag-source) true)))))

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
          (receive
            (drago {:containers [(dom/getElement "toolbox")
                                 (dom/getElement "dropzone")]})
            handler))))))

(defcard
  "## Toolbox Example

  We can bind functions to state changes using the receive function. Any function
  bound this way receives the new and previous state. This can be used for additional
  rendering, or side effects.

  We can create a simple toolbox ui by listening with a function.

  ```clojure
  (defn handle-drop [state _]
    (let [name (get-in state [:message :name])
          drop-target (get state :drop-target)
          drag-source (get state :drag-source)]
      (when (and (= :release name) (:container drop-target))
        (dom/append
          (:container drop-target)
          (.cloneNode (:element drag-source) true)))))

  ;; create a drag context and listen for state changes
  (-> (drago {:containers [toolbox dropzone]})
      (receive handle-drop))
  ```"
  (toolbox handle-drop))
