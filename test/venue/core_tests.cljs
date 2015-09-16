(ns venue.core-tests
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.test :refer-macros [async deftest is testing]]
            [cljs.core.async :as async :refer [chan put! <!]]
            [venue.core :as venue :include-macros true]))

(deftest get-route
  (let [route1 "/test"]
    (venue/defview! {:target "app" :route route1 :id :test-view})
    (is (= (venue/get-route :test-view) (str "#" route1)))))
