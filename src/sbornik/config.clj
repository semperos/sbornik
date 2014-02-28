(ns sbornik.config
  (:require [clojure.java.io :as io]
            [clojure.tools.reader.edn :as edn]
            [clojure.core.memoize :as memo]))

(def ^:dynamic *env* (or (System/getenv "SBORNIK_ENV")
                         :development))

(defn metadata* []
  (edn/read-string (slurp (io/reader (io/resource "metadata.edn")))))

(def metadata (memo/memo metadata*))
