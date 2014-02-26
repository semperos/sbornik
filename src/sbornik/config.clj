(ns sbornik.config)

(def ^:dynamic *env* (or (System/getenv "SBORNIK_ENV")
                         :development))
