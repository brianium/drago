(ns drago.reduce
  (:import goog.math.Coordinate))

(defn press
  [{:keys [target point] :as state}]
  (let [rect (.getBoundingClientRect target)]
    (-> state
      (assoc :pressed true)
      (assoc :rect rect)
      (assoc :offset (Coordinate. (- (.-x point) (.-left rect))
                                  (- (.-y point) (.-top rect)))))))

(defn move
  [{:keys [point offset] :as state}]
  (-> state
      (assoc :dragging true)
      (assoc :x (- (.-x point) (.-x offset)))
      (assoc :y (- (.-y point) (.-y offset)))))

(defn release
  [state]
  (assoc state :pressed false))

(defn reduce-state [state]
  (condp = (:name state)
    :begin (press state)
    :move (move state)
    :release (release state)
    state))
