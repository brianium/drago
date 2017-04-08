(ns drago.dom
  (:require [goog.dom.classlist :as classes]
            [goog.dom :as dom]))

(defn is-container?
  "Check if the dom element is a drago container"
  [element]
  (if element
    (classes/contains element "drago-container")
    false))

(defn belongs-to-container?
  "Check if the dom element's immediate parent is a drago container"
  [element]
  (let [parent (dom/getParentElement element)]
    (is-container? parent)))

(defn parent-container
  "Get the drago container that an element belongs to"
  [element]
  (dom/getAncestor element is-container?))

(defn find-container
  "Find the closest container to the element - including the element itself"
  [element]
  (cond
    (is-container? element) element
    (belongs-to-container? element) (parent-container element)
    :else nil))
