(ns sbornik.server
  (:require [org.httpkit.server :refer [run-server]]
            [sbornik.views :as v]
            [compojure.core :refer [defroute GET]]
            [compojure.route :as route]
            [compojure.handler :refer [site]])
  (:gen-class))

(defroutes app-routes
  (GET "/" (v/layout "Sbornik" :english "hello HTTP!"))
  (route/resources "/")
  (route/not-found (slurp (io/resource "404.html"))))

(def app ())

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
