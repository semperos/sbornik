(ns sbornik.bible
  (:require [clojure.java.io :as io]
            [clojure.string :refer [join]]
            [sbornik.config :as config]
            [sbornik.roman-time :refer [hours-for-date]]))

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
              (join "\n" ))))))
