(ns sbornik.api
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [liberator.core :refer [resource defresource]]
            [sbornik.config :as config]))

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

;;===============
;; Implementation

(defn chap-not-match
  [chap]
  (fn [line]
    (not (re-find (re-pattern (str "^#" chap)) line))))

(defn verse-not-match
  [verse]
  (fn [line]
    (not (re-find (re-pattern (str "^" verse "\\|")) line))))

(defn drop-til-start
  "Return lines starting at right point. Has chapter line, like #4, followed by first asked-for verse til end of text."
  [lines [start-chap start-verse]]
  (let [starting (drop-while (chap-not-match start-chap) lines)
        starting-chapter (first starting)]
    (concat [starting-chapter] (drop-while (verse-not-match start-verse) (next starting)))))

(defn take-til-end
  "Return lines up until the asked-for end."
  [lines [end-chap end-verse]]
  (let [ending (take-while (chap-not-match (inc end-chap)) lines)]
    (if (= end-verse :end)
      ending
      (reverse (drop-while (verse-not-match end-verse) (reverse ending))))))

(defn bible-lines
  [book-resource start end]
  (let [[start-chap start-verse] start
        [end-chap end-verse] end
        lines (line-seq (io/reader book-resource))
        starting (drop-til-start lines start)]
    (if (= end-chap :end)
      starting
      (take-til-end starting end))))

(defn bible-excerpt
  "Given component parts, build the path to a Bible text resource."
  ([{:keys [lang edition book start end]
     :or {lang "en"
          start [1 1]
          end [:end :end]}}]
     (when-let [books (get-in (config/metadata) (mapv keyword [lang :bible edition :books]))]
       (let [book-resource (->> books
                                (filter #(= (:name %) book))
                                :file
                                io/resource)]
         (->> (bible-lines book-resource start end)
              (filter identity)
              (str/join "\n" ))))))

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

(defresource metadata []
  :available-media-types ["application/edn"]
  :allowed-methods [:get]
  :exists? (fn [ctx] {::entry (config/metadata)})
  :handle-not-found (default-not-found)
  :handle-ok #(::entry %))
