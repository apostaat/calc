(ns calculator.calc
  (:require [schema.core :as s]
            [environ.core :refer [env]]
            [clojure.java.jdbc :as db]
            [compojure.api.sweet :refer [defroutes GET POST]]))

(defn reorder-equation [arg]
  (if (seq? arg)
    (let [[f s & r] arg
          f (reorder-equation f)]
      (cond
        (#{"*" "/"} (str s)) (let [[t ft & r2] r
                                   t (reorder-equation t)]
                               (if ft
                                 (list ft (list s f t) (reorder-equation r2))
                                 (list s f t)))
        (nil? s) f
        :else (list s f (reorder-equation r))))
    arg))

(defn calculate [inp]
  (-> (str "(" inp ")")
      read-string
      reorder-equation
      eval
      float))

(defn expression-valid? [e]
  (re-matches #"[\d+\s\-*\/()]+" e))

(def db
  {:dbtype "postgresql"
   :dbname (env :dbname)
   :host "localhost"
   :user (env :user)
   :password (env :password)})

(def calc-sql (db/create-table-ddl :calculations
                                   [[:expression "TEXT"]
                                    [:result "FLOAT"]]))

(defn run-new-db []
  (db/db-do-commands db [calc-sql]))

(defn retrieve-calc []
  (db/query db ["SELECT * FROM calculations"]))

(defn insert-calc [e r]
  (db/insert! db :calculations {:expression e
                           :result r}))

(s/defschema CalcExpression {:expression (s/pred expression-valid? s/Str)})

(defroutes calc-routes
  []
  (POST "/calc" req
    :summary "Calculate expression"
    :body [body CalcExpression]
    :return {:result s/Num}
    {:status 200
     :body (when-let [result (calculate (:expression body))]
             (insert-calc (:expression body) result)
             {:result result})})

  (GET "/calc" req
    :summary "Get history of calculations"
    :return [{:expression s/Str :result s/Num}]
    {:status 200
     :body (retrieve-calc)}))

