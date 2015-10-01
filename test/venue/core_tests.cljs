(ns venue.core-tests
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.test :refer-macros [async deftest is testing]]
            [cljs.core.async :as async :refer [chan put! <!]]
            [venue.core :as venue :include-macros true]))

(deftest get-route
  (let [route1 "/test"]
    (venue/defview! {:target "app" :route route1 :id :test-view})
    (is (= (venue/get-route :test-view) (str "#" route1)))))

(deftest service-init
  (let [init? (atom false)
        service (fn []
                  (reify
                    venue/IHandleRequest
                    (handle-request [owner request args response-ch])
                    venue/IInitialise
                    (initialise [owner cursor] (reset! init? true))))]
    (venue/defservice! {:id :service/test :handler service})
    (venue/start!)
    (is init?)))
