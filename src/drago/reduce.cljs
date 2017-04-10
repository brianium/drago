(ns drago.reduce
  (:require [goog.dom :as dom]
            [goog.dom.classlist :as classes]
            [goog.style :as style]
            [drago.dom :refer [find-container]])
  (:import goog.math.Coordinate))

(defn begin
  "Sets up initial state for drag operations. The drag target is cloned here for performance reasons
   and passed as part of the drag state"
  [{{{:keys [target point]} :body} :message :as state}]
  (let [rect (.getBoundingClientRect target)
        clone (.cloneNode target true)]
    (classes/add clone "drago-mirror")
    (-> state
        (assoc :dragging true)
        (assoc :mirror clone)
        (assoc :drag-source {:element target
                             :document (dom/getOwnerDocument target)
                             :rect rect
                             :offset (Coordinate. (- (.-x point) (.-offsetLeft target))
                                                  (- (.-y point) (.-offsetTop target)))}))))

(defn move
  "Update state based on movement"
  [{:keys [drag-source dragging] :as state}]
  (let [offset (:offset drag-source)
        {{:keys [target point element]} :body} (:message state)]
    (if-not dragging
      state
      (-> state
        (assoc-in [:drag-source :x] (- (.-x point) (.-x offset)))
        (assoc-in [:drag-source :y] (- (.-y point) (.-y offset)))
        (assoc :drop-target { :element element })
        (as-> state (if-let [container (find-container element)]
                      (assoc-in state [:drop-target :container] container)
                      state))))))

(defn release
  "Updates state when the pointer is released"
  [state]
  (assoc state :dragging false))

(defn reduce-state
  "The main state reducer. The state of a drag operation at any
   given time is produced by this function"
  [{{:keys [name]} :message :as state}]
  (condp = name
    :begin (begin state)
    :move (move state)
    :release (release state)
    state))
