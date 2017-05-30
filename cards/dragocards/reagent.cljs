(ns dragocards.reagent
  (:require [devcards.core]
            [reagent.core :as reagent]
            [goog.dom :as dom]
            [goog.dom.classlist :as classes]
            [drago.dnd.core :as dnd])
  (:require-macros [devcards.core :refer [defcard defcard-doc defcard-rg]]))


(defn dropzone-component
  [& children]
  [:div.drago-container.card-container
   (map-indexed #(with-meta %2 {:key %1}) children)])


(defn rectangle-component []
  [:div.rectangle])


(defn publish-container
  [context element]
  (dnd/add-container! context (reagent/dom-node element)))


(defn pointer-detection [drag-context]
  [:div.drag-demo
   [(with-meta dropzone-component {:component-did-mount #(publish-container drag-context %1)})
    [rectangle-component]]
   [(with-meta dropzone-component {:component-did-mount #(publish-container drag-context %1)})
    [rectangle-component]]])


(defcard-rg basic-pointer-detection
  "## Basic pointer detection"
  (fn [] (pointer-detection (dnd/start))))
