(ns calculator.handler
  (:require [compojure.api.sweet :refer [api routes]]
            [calculator.calc :refer [calc-routes]]
            [calculator.utils :as u]
            [compojure.route :as croutes]))



(defn not-found []
  (croutes/not-found {:error "No matching endpoint"}))

(def app
  (api
   {:exceptions {:handlers u/default-handlers}}
   (routes calc-routes
           (not-found))))

(def handler
  (routes app)) 