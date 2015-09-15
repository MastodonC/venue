(ns venue.core
  (:require
   [clojure.string :as str]
   [cljs.core]))

(defmacro defview!
  [{:keys [target view view-model id state route]}]
  `(define-fixtures!
     {:target ~target}
     [{:route ~route
       :id ~id
       :view (cljs.core/fn [] ~view)
       :view-model ~view-model
       :state ~state}]))

(defmacro defviews!
  [args views]
  (let [nviews (gensym 'nviews)]
    `(let [~nviews (mapv #(-> % (assoc :view (cljs.core/fn [] (:view %)))) ~views)]
       (define-fixtures!
         ~args
         ~nviews))))
