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
  [{:keys [point offset dragging target] :as state}]
  (cond-> state
    dragging
      (-> (assoc :x (- (.-x point) (.-x offset)))
          (assoc :y (- (.-y point) (.-y offset))))
    (and dragging (classes/contains target "drago-container"))
      (assoc :container target)))

(defn release
  [state]
  (assoc state :dragging false))

(defn leave
  [{:keys [data dragging] :as state}]
  (let [{:keys [previous]} data]
    (if dragging
      (-> (assoc state :previous-container previous)
          (dissoc :container))
      state)))

(defn reduce-state [state]
  (condp = (:name state)
    :begin (begin state)
    :move (move state)
    :release (release state)
    :leave (leave state)
    state))
