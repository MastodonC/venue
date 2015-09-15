(ns ^:figwheel-always venue.macros
    (:require [secretary.core :as secretary]))

(defmacro defroute!
  [route kroute routes funcn]
  (let [sec-obj (gensym route)]
    `(do
       (secretary/defroute ~sec-obj ~route []
         (~funcn ~route))
       (swap! ~routes assoc-in [~kroute] ~sec-obj))))
