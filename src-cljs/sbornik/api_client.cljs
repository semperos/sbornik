(ns sbornik.api-client
  (:require [cljs-http.client :as http]))

(defn get-metadata [] (http/get "/metadata"))

(defn get-bible-books
  ([] (get-bible-books "en"))
  ([lang] (get-bible-books lang "brenton"))
  ([lang edition]
     (http/get (str "/bible/" lang "/" edition "/books"))))
