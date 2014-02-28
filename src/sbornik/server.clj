(ns sbornik.server
  (:require [clojure.java.io :as io]
            [org.httpkit.server :refer [run-server]]
            [sbornik.views :as v]
            [sbornik.api :as api]
            [ring.middleware.reload :refer [wrap-reload]]
            [compojure.core :refer [defroutes GET ANY]]
            [compojure.route :as route]
            [compojure.handler :refer [site]]
            [sbornik.config :refer [*env*]])
  (:gen-class))

(defroutes app-routes
  (GET "/" [] (v/layout "Sbornik" :english))
  (ANY "/metadata" _ (api/metadata))
  (ANY "/bible/:lang/:edition/books/:book" {params :params} (api/bible-text params))
  (route/resources "/")
  (route/not-found (slurp (io/resource "404.html"))))

(def app (if (= *env* :development)
           (wrap-reload (site #'app-routes))
           (site app-routes)))

(defonce server (atom nil))

(defn start
  ([] (start server))
  ([server]
     (reset! server (run-server #'app {:port 8090}))))

(defn stop
  ([] (stop server))
  ([server]
     (when-not (nil? @server)
       (@server :timeout 100)
       (reset! server nil))))

(defn -main [& args]
  (println "Starting Sbornik server...")
  (start))
