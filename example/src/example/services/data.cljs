(ns example.services.data
  (:require [venue.core :as venue]
            [cljs.core.async :refer [chan close! put!]])
  (:require-macros [cljs-log.core :as log]
                   [cljs.core.async.macros :as m :refer [go]]))

(defn timeout [ms]
  (let [c (chan)]
    (js/setTimeout (fn [] (close! c)) ms)
    c))


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
  (let [wait 6000]
    (log/debug "Got login request. Waiting" wait "ms...")
    (go
      (<! (timeout wait))
      (log/debug "Finished waiting.")
      (put! result-ch [:success "OK"]))))
