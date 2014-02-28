(ns sbornik.app
  (:require [goog.events :as events]
            [cljs.core.async :refer [<!]]
            [clojure.browser.repl :as repl]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [secretary.core :as secretary :refer-macros [defroute]]
            [sbornik.api-client :as api])
  (:require-macros [cljs.core.async.macros :refer [go]])
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
  (reify
    om/IInitState
    (init-state [_]
      {:chans {:metadata (api/get-metadata)}})
    om/IWillMount
    (will-mount [_]
      (go (let [metadata-resp (<! (om/get-state owner [:chans :metadata]))]
            (om/update! app :metadata (:body metadata-resp)))))
    om/IRenderState
    (render-state [_ _]
      (html [:section
         [:h1 "Orthodox Sbornik"]
         (case (:showing app)
           :home [:div
                  [:h2 "Home"]
                  (when-let [bible-books (get-in app [:metadata :en :bible :brenton :books])]
                    [:ul
                     (for [book bible-books]
                       [:li book])])])]))))

(defn main []
  (om/root main-component page-state {:target (by-id "sbornik-app")}))

(js/jQuery
 (fn []
   ;; Application
   (main)
   ;; DO NOT COMMIT Dev time only. Comment this out before committing.
   (repl/connect "http://localhost:9000/repl")
   ))
