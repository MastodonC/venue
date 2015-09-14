(ns test-app.core
  (:require [venue.core :as venue]
            ;;
            [test-app.home.view]
            [test-app.home.view-model]
            [test-app.submit.view]
            [test-app.submit.view-model]
            [test-app.menu.view]
            [test-app.menu.view-model]))

(defonce init
  (do
    (venue/define-fixtures!
      {:target "app"}
      [{:route "/"
        :id :views/home
        :view test-app.home.view/view
        :view-model test-app.home.view-model/handler
        :state {:text "Hola hola"}}

       {:route "/submit"
        :id :views/submit
        :view test-app.submit.view/view
        :view-model test-app.submit.view-model/handler
        :state {:text "I am sparta"}}])

    (venue/define-static!
      {:target "menu"
       :id :menu
       :view test-app.menu.view/view
       :view-model test-app.menu.view-model/handler
       :state {}})

    (venue/start!)))
