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
          state {:message {:body {:target element :point point}}}
          new-state (begin state)
          clone (:mirror new-state)]
      (is (classes/contains clone "drago-mirror"))
      (is (= "8px" (.. clone -style -left)))
      (is (= "8px" (.. clone -style -top)))))

  (testing "drag state is set"
    (let [element (dom/getElement "clickable")
          point (Coordinate. 27 32)
          state {:message {:body {:target element :point point}}}
          new-state (begin state)
          drag-source (:drag-source new-state)]
      (is (:dragging new-state))
      (is (= element (:element drag-source)))
      (is (= 19 (.-x (:offset drag-source))))
      (is (= 24 (.-y (:offset drag-source))))
      (is (= 8 (.-left (:rect drag-source))))
      (is (= 8 (.-top (:rect drag-source)))))))

(deftest move-test
  (testing "state is returned unmodified if dragging not set"
    (let [point (Coordinate. 27 32)
          offset (Coordinate. 19 24)
          element (dom/getElement "clickable")
          state {:message {:body {:point point
                                  :target element}}
                 :drag-srouce {:offset offset}
                 :dragging false}
          new-state (move state)]
      (is (= state new-state))))

  (testing "x and y fields are added for move points if dragging set"
    (let [point (Coordinate. 27 32)
          offset (Coordinate. 19 24)
          element (dom/getElement "clickable")
          state {:message {:body {:point point
                                  :target element
                                  :element element}}
                 :drag-source {:offset offset}
                 :dragging true}
          new-state (move state)
          drag-source (:drag-source new-state)]
      (is (= (- 27 19) (:x drag-source)))
      (is (= (- 32 24) (:y drag-source)))))

  (testing "container element is added to state if dragging and present"
    (let [point (Coordinate. 1 1)
          offset (Coordinate. 1 1)
          element (dom/getElement "clickable")
          state {:message {:body {:point point
                                  :element element}}
                 :drag-source {:offset offset}
                 :dragging true}
          _  (classes/add element "drago-container")
          new-state (move state)
          drop-target (:drop-target new-state)]
      (is (= (:container drop-target) element))
      (is (= (:element drop-target) element))))

  (testing "container element is not added to state if not dragging"
    (let [point (Coordinate. 1 1)
          offset (Coordinate. 1 1)
          element (dom/getElement "clickable")
          state {:message {:body {:point point
                                  :target element}}
                 :drag-source {:offset offset}
                 :dragging false}
          new-state (move state)
          drop-target (:drop-target new-state)]
      (classes/add element "drago-container")
      (is (false? (contains? drop-target :container))))))

(deftest release-test
  (testing "it unsets dragging state"
    (let [new-state (release {:dragging true})]
      (is (false? (:dragging new-state))))))

