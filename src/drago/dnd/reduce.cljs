(ns drago.dnd.reduce
  (:require [goog.dom :as dom]
            [goog.dom.classlist :as classes]
            [goog.style :as style]
            [drago.dnd.container :as container])
  (:refer-clojure :exclude [reduce])
  (:import goog.math.Coordinate))


(defn init
  "Sets up initial state for drag operations."
  [{{{:keys [target point]} :body} :message :as state}]
  (let [rect (.getBoundingClientRect target)]
    (-> state
        (assoc :dragging true)
        (dissoc :drop-target)
        (assoc :drag-source {:element target
                             :document (dom/getOwnerDocument target)
                             :rect rect
                             :offset (Coordinate. (- (.-x point) (.-left rect))
                                                  (- (.-y point) (.-top rect)))}))))


(defn- add-mirror
  "Adds a mirror element of the drag source to state"
  [{{{:keys [target]} :body} :message :as state}]
  (let [clone (.cloneNode target true)]
    (classes/add clone "drago-mirror")
    (assoc state :mirror clone)))


(defn clone-if-configured
  "Adds a mirror element only if drago has been configured
  to use default rendering behavior"
  [{:keys [config] :as state}]
  (if-not (:render config)
    state
    (add-mirror state)))


;; Sets state relevant to beginning a drag operation
(def begin
  (comp
    clone-if-configured
    init))


(defn move
  "Update state based on movement"
  [{:keys [drag-source config] :as state}]
  (let [offset                                 (:offset drag-source)
        containers                             (:containers config)
        {{:keys [target point element]} :body} (:message state)]
    (-> state
        (assoc-in [:drag-source :x] (- (.-x point) (.-x offset)))
        (assoc-in [:drag-source :y] (- (.-y point) (.-y offset)))
        (assoc :drop-target { :element element })
        (as-> state (if-let [container (container/find-container containers element)]
                      (assoc-in state [:drop-target :container] container)
                      state)))))


(defn release
  "Updates state when the pointer is released"
  [state]
  (-> state
      (assoc :dragging false)
      (dissoc :drag-source)
      (dissoc :mirror)))


(defn reduce
  "The main state reducer. The state of a drag operation at any
   given time is produced by this function"
  [{{:keys [name]} :message :as state}]
  (condp = name
    :begin (begin state)
    :move (move state)
    :release (release state)
    state))
