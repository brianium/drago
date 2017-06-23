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


(defn basic-pointer-example []
  (dnd-app
    (fn [drag-context]
      [:div.drag-demo
       [(drop-target container drag-context)
        [rectangle]]
       [(drop-target container drag-context)
        [rectangle]]])))


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


(defn nested-containers-example []
  (dnd-app
    (fn [drag-context]
      [:div.drag-demo
       [(drop-target container drag-context)
        [rectangle]
        [(drop-target container drag-context)]]
       [(drop-target container drag-context)
        [rectangle]
        [(drop-target container drag-context)]]])))


(defcard-rg nested-containers
  "## Nested containers

  Nested containers are also a snap. Any container component that shares the
  same drag context will by default be enlisted in drag operations.

  ```clojure
  (defn nested-containers-example []
    (dnd-app
      (fn [drag-context]
        [:div.drag-demo
          [(drop-target container drag-context)
          [rectangle]
          [(drop-target container drag-context)]]
        [(drop-target container drag-context)
          [rectangle]
          [(drop-target container drag-context)]]])))
  ```"
  [nested-containers-example])


(def tools (reagent/atom []))


(defn selection [drag-context]
  [(drop-target container drag-context)
   (->> @tools
     (map #(vector :div.tool %1))
     (map-indexed #(with-meta %2 {:key (str "tool-" %1)})))])


(defn- tool-on-drop
  [text component]
  (let [element (reagent/dom-node component)]
    (fn [_ prev-state]
      (when (= (get-in prev-state [:drag-source :element]) element)
        (swap! tools conj text)))))


(defn tool
  [text drag-context]
  (reagent/create-class
    {:component-did-mount
     #(dnd/on-drop drag-context (tool-on-drop text %1))

     :display-name "tool"

     :reagent-render
     (fn [text _]
       [:div.tool text])}))


(defn toolbox-example []
  (dnd-app
    (fn [drag-context]
      [:div.drag-demo
       [(drop-target container drag-context)
        [tool "tool 1" drag-context]
        [tool "tool 2" drag-context]
        [tool "tool 3" drag-context]]
       (selection drag-context)])))


(defcard-rg toolbox
  "## Toolbox example

  In this example we render a drag and drop container based off state
  stored in a reagent atom. We use a couple of functions to wire everything
  up to the drag context and the reagent atom.

  ```clojure
  (def tools (reagent/atom []))
  
  
  (defn selection [drag-context]
    [(drop-target container drag-context)
     (->> @tools
       (map #(vector :div.tool %1))
       (map-indexed #(with-meta %2 {:key (str \"tool-\" %1)})))])
  
  
  (defn- tool-on-drop
    [text component]
    (let [element (reagent/dom-node component)]
      (fn [_ prev-state]
        (when (= (get-in prev-state [:drag-source :element]) element)
          (swap! tools conj text)))))
  
  
  (defn tool
    [text drag-context]
    (reagent/create-class
      {:component-did-mount
       #(dnd/on-drop drag-context (tool-on-drop text %1))
  
       :display-name \"tool\"
  
       :reagent-render
       (fn [text _]
         [:div.tool text])}))
  
  
  (defn toolbox-example []
    (dnd-app
      (fn [drag-context]
        [:div.drag-demo
         [(drop-target container drag-context)
          [tool \"tool 1\" drag-context]
          [tool \"tool 2\" drag-context]
          [tool \"tool 3\" drag-context]]
         (selection drag-context)])))
  ```"
  [toolbox-example])
