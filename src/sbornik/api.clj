(ns sbornik.api
  (:require [clojure.set :refer [rename-keys]]
            [liberator.core :refer [resource defresource]]
            [clj-time.core :refer [date-time]]
            [sbornik.config :as config]
            [sbornik.bible :refer [bible-excerpt]]
            [sbornik.roman-time :refer [hours-for-date]]))

;;==========
;; Utilities

(defn to-number
  [x]
  (if (or (number? x)
          (= x :end))
    x
    (try
      (Integer/parseInt x)
      (catch NumberFormatException e
        nil))))

(defn default-not-found
  []
  (fn [ctx] {:status :not-found}))

;;==============
;; API Resources

(defresource metadata []
  :available-media-types ["application/edn"]
  :allowed-methods [:get]
  :exists? (fn [ctx] {::entry (config/metadata)})
  :handle-not-found (default-not-found)
  :handle-ok #(::entry %))

(defresource bible-text
  [{:keys [lang edition book start-chapter start-verse end-chapter end-verse]
    :or {start-chapter 1
         start-verse 1
         end-chapter :end
         end-verse :end}
    :as params}]
  :available-media-types ["application/edn"]
  :allowed-methods [:get]
  :exists? (fn [ctx]
             (when-let [excerpt (-> params
                                    (merge {:start [(to-number start-chapter)
                                                    (to-number start-verse)]
                                            :end [(to-number end-chapter)
                                                  (to-number end-verse)]})
                                    bible-excerpt)]
               {::entry {:bible-text excerpt}}))
  :handle-not-found (default-not-found)
  :handle-ok #(::entry %))

(defresource hours [{:keys [year month day state city dst] :as params}]
  :available-media-types ["application/edn"]
  :allowed-methods [:get]
  :exists? (fn [ctx] {::entry (hours-for-date (date-time year month day)
                                             (rename-keys params {:dst :dst?}))})
  :handle-not-found (default-not-found)
  :handle-ok #(::entry %))
