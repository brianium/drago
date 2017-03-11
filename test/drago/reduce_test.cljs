(ns drago.reduce-test
  (:require [cljs.test :refer-macros [deftest is testing use-fixtures]]
            [goog.dom :as dom]
            [goog.style :as style]
            [goog.dom.classlist :as classes]
            [drago.reduce :refer [begin move release]])
  (:import goog.math.Coordinate))

(use-fixtures :each
  {:before (fn []
             (let [element (dom/createElement "div")]
               (set! (.-id element) "clickable")
               (dom/appendChild (.-body js/document) element)
               (style/setPosition element 8 8)))
   :after (fn []
            (let [element (dom/getElement "clickable")]
              (dom/removeNode element)))})

(deftest begin-test
  (testing "element association via clone"
    (let [element (dom/getElement "clickable")
          point (Coordinate. 27 32)
          state {:target element :point point}
          new-state (begin state)
          clone (:mirror new-state)]
      (is (classes/contains clone "drago-mirror"))
      (is (= "8px" (.. clone -style -left)))
      (is (= "8px" (.. clone -style -top)))))

  (testing "drag state is set"
    (let [element (dom/getElement "clickable")
          point (Coordinate. 27 32)
          state {:target element :point point}
          new-state (begin state)]
      (is (:dragging new-state))
      (is (= element (:element new-state)))
      (is (= 19 (.-x (:offset new-state))))
      (is (= 24 (.-y (:offset new-state))))
      (is (= 8 (.-left (:rect new-state))))
      (is (= 8 (.-top (:rect new-state)))))))

(deftest move-test
  (testing "state is returned unmodified if dragging not set"
    (let [point (Coordinate. 27 32)
          offset (Coordinate. 19 24)
          state {:point point :offset offset :dragging false}
          new-state (move state)]
      (is (= state new-state))))

  (testing "x and y fields are added for move points if dragging set"
    (let [point (Coordinate. 27 32)
          offset (Coordinate. 19 24)
          state {:point point :offset offset :dragging true}
          new-state (move state)]
      (is (= (- 27 19) (:x new-state)))
      (is (= (- 32 24) (:y new-state))))))

(deftest release-test
  (testing "it unsets dragging state"
    (let [new-state (release {:dragging true})]
      (is (false? (:dragging new-state))))))

