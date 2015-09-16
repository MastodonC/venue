(ns venue.core-tests
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.test :refer-macros [async deftest is testing]]
            [cljs.core.async :as async :refer [chan put! <!]]
            [venue.core :as venue :include-macros true]))

(defn empty-view [& args])
(defn empty-handler [& args])

(deftest get-route
  (let [route1 "/test"])
  (venue/defview! {:target ""
                  :route route
                   :id :test-view
                   :view empty-view
                   :view-model empty-handler})
  (is (= (venue/get-route :test-view) route1)))
