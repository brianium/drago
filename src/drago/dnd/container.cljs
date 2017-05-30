(ns drago.dnd.container
  (:require [goog.dom.classlist :as classes]
            [goog.dom :as dom]))


(defn is-container?
  "Check if the dom element is a drago container"
  [containers element]
  (if element
    (some #(= % element) containers)
    false))


(defn belongs-to-container?
  "Check if the dom element's immediate parent is a drago container"
  [containers element]
  (if element
    (->> element
         dom/getParentElement
         (is-container? containers))
    false))


(defn parent-container
  "Get the drago container that an element belongs to"
  [containers element]
  (dom/getAncestor
    element
    #(is-container? containers %)))


(defn find-container
  "Find the closest container to the element - including the element itself"
  [containers element]
  (cond
    (is-container? containers element) element
    (belongs-to-container? containers element) (parent-container containers element)
    :else nil))
