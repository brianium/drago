(ns drago.view
  (:require [goog.dom :as dom]
            [goog.dom.classlist :as classes]
            [goog.style :as style]
            [goog.style.transform :as transform]))

(defn- in-iframe?
  [document]
  (not= document js/document))

(defn- get-frame
  [doc]
  (let [frame-name (.. doc -defaultView -name)]
    (when-not (empty? frame-name)
      (.querySelector js/document "[name=preview]"))))

(defn- offset-frame
  [x y frame]
  (let [rect (.getBoundingClientRect frame)
        fx (.-left rect)
        fy (.-top rect)]
    {:x (+ x fx) :y (+ y fy)}))

(defn- position
  [rect owner-document]
  (let [x (.-left rect)
        y (.-top rect)]
    (if-let [frame (get-frame owner-document)]
      (offset-frame x y frame)
      {:x x :y y})))

;;; Draw begin state
(defn- init-clone-position
  [{:keys [mirror rect owner-document]}]
  (let [{:keys [x y]} (position rect owner-document)]
    (style/setPosition mirror x y)))

(defn- append-element
  [{:keys [document mirror rect]}]
  (style/setSize mirror (.-width rect) (.-height rect))
  (dom/appendChild (.-body document) mirror))

(defn- add-start-classes
  [{:keys [element]}]
  (classes/add element "drago-dragging"))

(def begin
  (juxt init-clone-position append-element add-start-classes))

;;; Draw move state
(defn- position-element
  [{:keys [mirror x y rect]}]
  (when mirror
    (transform/setTranslation
      mirror
      (- x (.-left rect))
      (- y (.-top rect)))))

;;; Draw release state
(defn- remove-element
  [{:keys [mirror]}]
  (dom/removeNode mirror))

(defn- remove-start-classes
  [{:keys [element]}]
  (when element
    (classes/remove element "drago-dragging")))

(def release
  (juxt remove-element remove-start-classes))

(defn render [{:keys [name] :as data}]
  (case name
    :begin (begin data)
    :move (position-element data)
    :release (release data)
    ""))
