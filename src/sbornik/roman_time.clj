(ns ^{:doc "Utilities for calculating the Roman hours of the day."}
  sbornik.roman-time
  (:require [clojure.string :refer [split trim]]
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

(defn parse-long [^String s] (Long/parseLong s))

(defn process-line
  [state line]
  (let [date (parse-long (subs line 0 2))
        months {:jan {:sunrise-pos [4 8] :sunset-pos [9 13]}
                :feb {:sunrise-pos [15 19] :sunset-pos [20 24]}
                :mar {:sunrise-pos [26 30] :sunset-pos [31 35]}
                :apr {:sunrise-pos [37 41] :sunset-pos [42 46]}
                :may {:sunrise-pos [48 52] :sunset-pos [53 57]}
                :jun {:sunrise-pos [59 63] :sunset-pos [64 68]}
                :jul {:sunrise-pos [70 74] :sunset-pos [75 79]}
                :aug {:sunrise-pos [81 85] :sunset-pos [86 90]}
                :sep {:sunrise-pos [92 96] :sunset-pos [97 101]}
                :oct {:sunrise-pos [103 107] :sunset-pos [108 112]}
                :nov {:sunrise-pos [114 118] :sunset-pos [119 123]}
                :dec {:sunrise-pos [125 129] :sunset-pos [130 134]}}]
    (reduce (fn [state [month m]]
              (let [{:keys [sunrise-pos sunset-pos]} m
                    [sunrise-start sunrise-end] sunrise-pos
                    [sunset-start sunset-end] sunset-pos]
                (-> state
                    (update-in [month date :sunrise]
                                  (fn [_]
                                    (let [value (trim (subs line sunrise-start sunrise-end))]
                                      (when (seq value)
                                        {:hour (parse-long (subs value 0 2))
                                         :minute (parse-long (subs value 2))}))))
                       (update-in [month date :sunset]
                                  (fn [_]
                                    (let [value (trim (subs line sunset-start sunset-end))]
                                      (when (seq value)
                                        {:hour (parse-long (subs value 0 2))
                                         :minute (parse-long (subs value 2))})))))))
            state
            months)))

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
       (reduce process-line {} lines))))

(def month-nums
  {1  :jan 2  :feb 3  :mar
   4  :apr 5  :may 6  :jun
   7  :jul 8  :aug 9  :sep
   10 :oct 11 :nov 12 :dec})

(defn sunrise-sunset-for-date
  ([] (sunrise-sunset-for-date (dt/now)))
  ([date] (sunrise-sunset-for-date date {}))
  ([date {:keys [state city dst?]
          :or {state (:state *location*)
               city (:city *location*)
               dst? (:dst? *location*)}
          :as opts}]
     (let [all-year (sunrise-sunset-for-year (dt/year date) opts)
           [month day] ((juxt dt/month dt/day) date)]
       (get-in all-year [(get month-nums month) day]))))

(defn hours-for-date
  ([] (hours-for-date (dt/now)))
  ([date] (hours-for-date date {}))
  ([date {:keys [state city dst?]
          :or {state (:state *location*)
               city (:city *location*)
               dst? (:dst? *location*)}
          :as opts}]
     (let [{:keys [sunrise sunset]} (sunrise-sunset-for-date date)
           [year month day] ((juxt dt/year dt/month dt/day) date)
           sunrise (dt/date-time year month day (:hour sunrise) (:minute sunrise))
           sunset (dt/date-time year month day (:hour sunset) (:minute sunset))
           interval-secs (dt/in-seconds (dt/interval sunrise sunset))
           interval (/ interval-secs 12)
           hour-fn (fn [nth-hour] (dt/plus sunrise (dt/seconds (* nth-hour interval))))]
       {:interval (/ interval 60)
        :first (hour-fn 1)
        :second (hour-fn 2)
        :third (hour-fn 3)
        :fourth (hour-fn 4)
        :fifth (hour-fn 5)
        :sixth (hour-fn 6)
        :seventh (hour-fn 7)
        :eight (hour-fn 8)
        :ninth (hour-fn 9)
        :tenth (hour-fn 10)
        :eleventh (hour-fn 11)
        :twelfth sunset})))
