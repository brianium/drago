(ns drago.reduce-test
  (:require [cljs.test :refer-macros [deftest is testing use-fixtures]]
            [goog.dom :as dom]
            [goog.style :as style]
            [goog.dom.classlist :as classes]
            [drago.reduce :refer [begin]])
  (:import goog.math.Coordinate))

(use-fixtures :once
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
          clone (:element new-state)]
      (is (classes/contains clone "mirror"))
      (is (= "8px" (.. clone -style -left)))
      (is (= "8px" (.. clone -style -top)))))

  (testing "drag state is set"
    (let [element (dom/getElement "clickable")
          point (Coordinate. 27 32)
          state {:target element :point point}
          new-state (begin state)]
      (is (:dragging new-state))
      (is (= 19 (.-x (:offset new-state))))
      (is (= 24 (.-y (:offset new-state))))
      (is (= 8 (.-left (:rect new-state))))
      (is (= 8 (.-top (:rect new-state)))))))

