(ns sbornik.server-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :refer :all]
            [sbornik.server :refer :all]

            [clojure.string :as str]
            [clojure.tools.reader.edn :as edn]))

(def default-success {:status 200
                      :headers {"Vary" "Accept"
                                "Content-Type" "application/edn;charset=UTF-8"}})

(defn succeeds?
  ([resp] (succeeds? resp default-success))
  ([resp expected]
     (doseq [[k v] expected]
       (is (= (get resp k) v)))))

(deftest ^:integration test-bible-endpoint
  (testing "no query parameters"
    (let [resp (app (request :get "/bible/en/brenton/Amos"))]
      (succeeds? resp)
      (is (> (count (str/split (:body resp) #"\\n")) 160))
      (is (= (keys (edn/read-string (:body resp))) [:bible-text]))))
  (testing "start/end query parameters"
    (let [resp (app (request :get "/bible/en/brenton/Amos" {:start-chapter 1
                                                            :start-verse 1
                                                            :end-chapter 2
                                                            :end-verse 3}))]
      (succeeds? resp)
      (let [cnt (count (str/split (:body resp) #"\\n"))]
        (is (> cnt 1))
        (is (< cnt 25)))
      (is (= (keys (edn/read-string (:body resp))) [:bible-text])))))

(deftest ^:integration test-hours-endpoint
  (testing "no query parameters"
    (let [resp (app (request :get "/hours"))]
      (succeeds? resp)
      (is (every? (partial contains? (:body resp)) [:first :second :third
                                                    :fourth :fifth :sixth
                                                    :seventh :eigth :ninth
                                                    :tenth :eleventh :twelfth])))))
