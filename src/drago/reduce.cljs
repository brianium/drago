(ns drago.reduce
  (:require [goog.dom :as dom]
            [goog.dom.classlist :as classes]
            [goog.style :as style])
  (:import goog.math.Coordinate))

(defn- clone-node [elem]
  (.cloneNode elem true))

(defn begin
  "Sets up initial state for drag operations. The drag target is cloned here for performance reasons
   and passed as part of the drag state"
  [{:keys [target point] :as state}]
  (let [rect (.getBoundingClientRect target)
        clone (clone-node target)]
    (classes/add clone "drago-mirror")
    (-> state
        (assoc :dragging true)
        (assoc :mirror clone)
        (assoc :element target)
        (assoc :document (dom/getOwnerDocument target))
        (assoc :rect rect)
        (assoc :offset (Coordinate. (- (.-x point) (.-offsetLeft target))
                                    (- (.-y point) (.-offsetTop target)))))))
(defn move
  "Update state based on movement"
  [{:keys [point offset dragging target] :as state}]
  (if (not dragging)
    state
    (-> state
        (assoc :x (- (.-x point) (.-x offset)))
        (assoc :y (- (.-y point) (.-y offset)))
        (as-> state (if (classes/contains target "drago-container")
                      (assoc state :container target)
                      state)))))

(defn release
  "Updates state when the pointer is released"
  [{:keys [container] :as state}]
  (-> state
      (assoc :dragging false)
      (as-> state (if container
                    (assoc state :previous-container container)
                    state))
      (dissoc :container)))

(defn leave
  "Update state when the pointer leaves a drag container"
  [{:keys [data dragging] :as state}]
  (let [{:keys [previous]} data]
    (if dragging
      (-> (assoc state :previous-container previous)
          (dissoc :container))
      state)))

(defn reduce-state
  "The main state reducer. The state of a drag operation at any
   given time is produced by this function"
  [state]
  (condp = (:name state)
    :begin (begin state)
    :move (move state)
    :release (release state)
    :leave (leave state)
    state))
