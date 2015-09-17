(ns venue.core
  (:require
   [clojure.string :as str]
   [cljs.core]))

(defmacro defview!
  [{:keys [target view view-model id state route]
    :or   {static false}}]
  (let [v (gensym)]
    `(defonce ~v
       (do
         (add-view!
          {:target ~target
           :route ~route
           :id ~id
           :view (cljs.core/fn [] ~view)
           :view-model (cljs.core/fn [] ~view-model)
           :state ~state})))))

(defmacro defstatic!
  [{:keys [target view view-model id state]}]
  (let [v (gensym)]
    `(defonce ~v
       (do
         (add-static-view!
          {:target ~target
           :id ~id
           :view (cljs.core/fn [] ~view)
           :view-model (cljs.core/fn [] ~view-model)
           :state ~state})))))

(defmacro defservice!
  [service]
  `(add-service! ~service))
