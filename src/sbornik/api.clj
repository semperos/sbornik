(ns sbornik.api
  (:require [liberator.core :refer [resource defresource]]))

;; TODO
;;
;;  * Add resources for all the things Ponomar has
;;  * See what it'll take to use the Java files included in the Ponomar lib,
;;    consider just making a legit JAR as part of the process
(defresource bible-text [{:keys [lang edition book chapter verse]}])
