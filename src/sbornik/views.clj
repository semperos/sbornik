(ns sbornik.views
  (:require [clojure.string :refer [capitalize]]
            [clojure.java.io :as io]
            [hiccup.core :refer :all]
            [hiccup.page :refer [html5 include-css include-js]]
            [sbornik.config :refer [*env*]]))

(defn dev
  ([x] (when (= *env* :development) x))
  ([x y] (if (= *env* :development) x y)))

(defn layout [title lang & content]
  (html5
   {:lang lang}
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
    [:title title]
    (include-css
     "//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css"
     "//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap-theme.min.css"
     "/app/css/app.css")
    (include-js
     "//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"
     "http://fb.me/react-0.9.0.js")]
   [:body
    [:div#sbornik-app.container
     content

     (include-js
      (dev "/app/js/goog/base.js")
      (dev "/app/js/app-dev.js" "/app/js/app-prod.js"))
     [:script {:type "text/javascript"}
      "goog.require(\"sbornik.app\");"]]]))

(defn show-bible-chapter
  [lang edition chapter]
  (let [title (str "Bible, " edition " Chapter " chapter)
        file (str "ponomar/Ponomar/languages/"
                  lang "/bible/"
                  edition "/" chapter ".text")]
    (layout lang title (slurp (io/resource file)))))
