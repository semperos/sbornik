(ns ^{:doc "Utilities for calculating the Roman hours of the day."}
  sbornik.roman-time
  (:require [clojure.string :refer [split]]
            [clojure.core.memoize :as memo]
            [clj-http.client :as http]
            [clj-time.core :as dt]
            [net.cgrand.enlive-html :refer [html-snippet select]]))

(def ^:dynamic *location*
  {:state "SC"
   :city "Summerville"
   :dst? true})

(defn request-sunrise-sunset-data*
  [year state city]
  (http/post "http://aa.usno.navy.mil/cgi-bin/aa_rstablew.pl"
             {:headers {"Origin" "http://aa.usno.navy.mil"
                        "Accept-Encoding" "gzip,deflate,sdch"
                        "Accept-Language" "en-US,en;q=0.8,ru;q=0.6"
                        "Content-Type" "application/x-www-form-urlencoded"
                        "Accept" "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"
                        "Cache-Control" "max-age=0"
                        "Referer" "http://aa.usno.navy.mil/data/docs/RS_OneYear.php"
                        "Connection" "keep-alive"}
              :form-params {"FFX" 1
                            "xxy" year
                            "type" 0
                            "st" state
                            "place" city
                            "ZZZ" "END"}}))

(def request-sunrise-sunset-data (memo/memo request-sunrise-sunset-data*))

(defn tree-from-html
  "Create Enlive-compatible tree of DOM from response text."
  [html-text]
  (html-snippet
   (subs html-text (.indexOf ^String html-text "<html>"))))

(defn raw-table
  [year state city]
  (let [doc (tree-from-html (:body (request-sunrise-sunset-data* year state city)))
           node (select doc [:body :> :pre])]
       (-> node first :content first)))

(defn sunrise-sunset-for-year
  ([] (sunrise-sunset-for-year (dt/year (dt/now))))
  ([year] (sunrise-sunset-for-year year {}))
  ([year {:keys [state city dst?]
          :or {state (:state *location*)
               city (:city *location*)
               dst? (:dst? *location*)}}]
     (let [raw-table (raw-table year state city)
           lines (->> (split raw-table #"\n")
                      (remove empty?)
                      (drop 9)
                      butlast)]
       lines)))
