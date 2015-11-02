(ns example.services.data
  (:require [venue.core :as venue])
  (:require-macros [cljs-log.core :as log]))


(defmulti request-handler
  (fn [owner event args result-ch] event))

(defmulti response-handler
  (fn [owner result response context] result))

(defn service
  []
  (reify
    venue/IHandleRequest
    (handle-request [owner request args response-ch]
      (request-handler owner request args response-ch))
    venue/IHandleResponse
    (handle-response [owner outcome event response context]
      (response-handler owner [event outcome] response context))))

(defmethod request-handler
  :login
  [owner _ args result-ch]
  (log/debug "Got login request"))
