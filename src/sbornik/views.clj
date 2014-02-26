(ns sbornik.views
  (:require [hiccup.core :refer :all]
            [hiccup.page :refer [html5 include-css include-js]]
            [sbornik.config :refer [*env*]]))

(def ^{:doc "Languages supported by the site."}
  languages
  {:english "en"
   :russian "ru"})

(defn layout [title lang-kw & content]
  (html5
   {:lang (lang-kw languages)}
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
    [:title title]
    (include-css
     "//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css"
     "//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap-theme.min.css"
     "/app/css/app.css")]
   [:body
    [:div#sbornik-app.container
     content

     (include-js
      "http://fb.me/react-0.9.0.js"
      (when (= *env* :development)
        "out/goog/base.js"
        "/app/js/goog/base.js"
        "/app/js/app-dev.js"))
     [:script {:type "text/javascript"}
      "goog.require(\"sbornik.app\");"]]]))
