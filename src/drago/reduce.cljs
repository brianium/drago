(ns drago.reduce
  (:require [goog.dom.classlist :as classes]
            [goog.style :as style])
  (:import goog.math.Coordinate))

(defn- clone-node [elem]
  (.cloneNode elem true))

(defn- position-clone
  [clone rect]
  (style/setPosition
    clone
    (.-left rect)
    (.-top rect)))

(defn begin
  "Sets up initial state for drag operations. The drag target is cloned here for performance reasons
   and passed as part of the drag state"
  [{:keys [target point] :as state}]
  (let [rect (.getBoundingClientRect target)
        clone (clone-node target)]
    (classes/add clone "mirror")
    (position-clone clone rect)
    (-> state
        (assoc :dragging true)
        (assoc :element clone)
        (assoc :rect rect)
        (assoc :offset (Coordinate. (- (.-x point) (.-left rect))
                                    (- (.-y point) (.-top rect)))))))
(defn move
  [{:keys [point offset dragging] :as state}]
  (if dragging
    (-> state
        (assoc :x (- (.-x point) (.-x offset)))
        (assoc :y (- (.-y point) (.-y offset))))
    state))

(defn release
  [state]
  (assoc state :dragging false))

(defn reduce-state [state]
  (condp = (:name state)
    :begin (begin state)
    :move (move state)
    :release (release state)
    state))
