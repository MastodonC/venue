(ns test-app.core
  (:require [venue.core :as venue]
            ;;
            [test-app.view-models.hello]
            [test-app.views.hello]
            [test-app.view-models.goodbye]
            [test-app.views.goodbye]
            [test-app.view-models.menu]
            [test-app.views.menu]))

(venue/define-fixtures!
  {:target "app"}
  [{:route "/"
    :id :views/home
    :view test-app.views.hello/view
    :view-model test-app.view-models.hello/handler
    :state {:text "Hola hola"}}

   {:route "/goodbye"
    :id :views/goodbye
    :view test-app.views.goodbye/view
    :view-model test-app.view-models.goodbye/handler
    :state {:text "I am sparta"}}])

(venue/define-static!
  {:target "menu"
   :id :menu
   :view test-app.views.menu/view
   :view-model test-app.view-models.menu/handler
   :state {}})

(venue/start!)
