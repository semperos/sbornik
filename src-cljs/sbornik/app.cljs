(ns sbornik.app
  (:require [goog.events :as events]
            [clojure.browser.repl :as repl]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [secretary.core :as secretary :refer-macros [defroute]])
  (:import [goog History]
           [goog.History EventType]))

;; =============================================================================
;; Routing
(def page-state (atom {:showing :home}))

(defroute "/" [] (swap! page-state assoc :showing :home))

(defroute "/:filter" [filter] (swap! page-state assoc :showing (keyword filter)))

(def history (History.))

(events/listen
 history EventType.NAVIGATE
 (fn [e] (secretary/dispatch! (.-token e))))

(.setEnabled history true)

;;==========
;; Utilities

(defn by-id
  [id]
  (.getElementById js/document id))

;;===========
;; Components

(defn main-component
  "Establish a root parent node, render page template into it, then initialize individual sections/components which may or may not rely on external data loaded asynchronously."
  [app owner]
  (om/component
   ;; (dom/section nil
   ;;   (dom/h1 nil "Orthodox Sbornik")
   ;;   (case (:showing app)
   ;;     :home (dom/div nil
   ;;             (dom/h2 "Home"))))
   (html [:section
          [:h1 "Orthodox Sbornik"]
          (case (:showing app)
            :home [:div
                   [:h2 "Home"]])])
   ))

(defn main []
  (om/root main-component page-state {:target (by-id "sbornik-app")}))

(js/jQuery
 (fn []
   ;; Application
   (main)
   ;; DO NOT COMMIT Dev time only. Comment this out before committing.
   (repl/connect "http://localhost:9000/repl")
   ))
