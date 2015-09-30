(ns example.services.data
  (:require [venue.core :as venue])
  (:require-macros [cljs-log.core :as log]))

(defmulti response-handler
  (fn [result response cursor] result))

(defmulti request-handler
  (fn [event args response-ch] event))

(defn service
  []
  (reify
    venue/IHandleRequest
    (handle-request [owner request args response-ch]
      (request-handler request args response-ch))
    venue/IHandleResponse
    (handle-response [owner outcome event response cursor]
      (response-handler [event outcome] response cursor))
    venue/IInitialise
    (initialise [owner cursor]
      (log/debug "DATA SERVICE INIT"))))
