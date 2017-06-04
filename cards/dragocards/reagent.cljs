(ns dragocards.reagent
  (:require [devcards.core]
            [reagent.core :as reagent]
            [goog.dom :as dom]
            [goog.dom.classlist :as classes]
            [drago.dnd.core :as dnd])
  (:require-macros [devcards.core :refer [defcard defcard-doc defcard-rg]]))


(defn publish-container
  [context element]
  (dnd/add-container! context (reagent/dom-node element)))


(defn drop-target
  [component context]
  (with-meta
    component
    {:component-did-mount #(publish-container context %1)}))


(defn container
  [& children]
  [:div.drago-container.card-container
   (map-indexed #(with-meta %2 {:key %1}) children)])


(defn rectangle []
  [:div.rectangle])


(defn dnd-app
  [component]
  [component (dnd/start)])


(defn basic-pointer-example []
  (dnd-app
    (fn [drag-context]
      [:div.drag-demo
       [(drop-target container drag-context)
        [rectangle]]
       [(drop-target container drag-context)
        [rectangle]]])))


(defcard-doc
  "## Drago and Reagent

  Using drago with Reagent is not difficult. These examples use a handful of functions
  as defined below. These functions could easily make up a wrapper library :)

  ```clojure
  ;;; demo components
  (defn container
    [& children]
    [:div.drago-container.card-container
     (map-indexed #(with-meta %2 {:key %1}) children)])
  
  
  (defn rectangle []
    [:div.rectangle])


  ;;; library functions
  (defn publish-container
    \"pushes a reagent element's dom node into a drago context\"
    [context element]
    (dnd/add-container! context (reagent/dom-node element)))
  
  
  (defn drop-target
    \"registers a component as a drop target - i.e makes the reagent element's
     dom node a drago container\"
    [component context]
    (with-meta
      component
      {:component-did-mount #(publish-container context %1)}))


  (defn dnd-app
    \"a higher order component that provides a drago context - this should really
     only be called once at the highest point of a component tree\"
    [component]
    [component (dnd/start)])
  ```")


(defcard-rg basic-pointer-detection
  "## Basic pointer detection

  This example demonstrates a pretty basic example of using drago with
  reagent. In many ways it is simpler than a vanilla js approach.

  ```clojure
  (defn basic-pointer-example []
    (dnd-app
      (fn [drag-context]
        [:div.drag-demo
          [(drop-target container drag-context)
           [rectangle]]
          [(drop-target container drag-context)
           [rectangle]]])))
  ```"
  [basic-pointer-example])
