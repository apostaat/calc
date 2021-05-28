(ns calculator.handler-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [ring.mock.request :as mock]
            [calculator.handler :refer [app]]
            [calculator.calc :refer [calculate db]]
            [clojure.java.jdbc :as db]
            [calculator.utils :as u]))

(defn fix-clean-db [t]
  (db/db-do-commands db ["TRUNCATE TABLE calculations"])
  (t))

(use-fixtures :each fix-clean-db)

(deftest calculation-tests
  (testing 
   "ordinary operations go normal"
   (is (= 5.0 (calculate "2 + 3")))
   (is (= 10.0 (calculate "2 * 5")))
   (is (= -3.0 (calculate "2 - 5")))
   (is (= 5.0 (calculate "10 / 2"))))
  (testing
   "associative operations go normal"
    (is (= 11.0 (calculate "2 + 3 * 3")))
    (is (= -10.0 (calculate "2 + (3 - 5 * 3)")))
    (is (= 3.0 (calculate "-15 / (10 - 5 * 3)")))))

(deftest test-app
  (testing "we retrieve results and they are empty "
    (let [empty-calc (-> (mock/request :get "/calc")
                         app
                         u/response)]
      (is (= 200 (:status empty-calc)))
      (is (= [] (:body empty-calc)))))

  (testing "we do some calculations"
    (let [calc-one (-> (mock/request :post "/calc")
                       (u/json-body-enc {:expression "1 + 2 + 3"})
                       app
                       u/response)]
      (is (= 200 (:status+ calc-one)))
      (is (= 6.0 (-> calc-one :body :result)))))
  
  (testing "when we have something that may break our program it fails"
    (let [calc-fail (-> (mock/request :post "/calc")
                       (u/json-body-enc {:expression "1 + 1 nil "})
                       app
                       u/response)]
      (is (= 400 (:status calc-fail)))))
  
  (testing "so we retrieve results and get them, previous entry is not in db"
    (let [non-empty-calc (-> (mock/request :get "/calc")
                             app
                             u/response)]
      (is (= 200 (:status+ non-empty-calc)))
      (is (= [{:expression "1 + 2 + 3"
               :result 6.0}] (:body non-empty-calc)))))

  (testing "not-found route"
    (let [invalid-route (-> (mock/request :get "/invalid")
                            app
                            u/response)]
      (is (= 404 (:status invalid-route))))))
