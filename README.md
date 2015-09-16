# Venue

Venue is an MVVM-inspired ClojureScript framework for creating [single-page applications](https://en.wikipedia.org/wiki/Single-page_application). It uses Om, Secretary, DataScript and a handful of other CLJS libraries. It's opinionated and expects applications to adhere to the principles of [MVVM] (https://en.wikipedia.org/wiki/Model_View_ViewModel): databinding is provided by Om/React, models are ~~facilitated by DataScript~~ coming soon.

#### A route with a view

```clojure
(ns test-app.core
  (:require [venue.core :as venue :include-macros true]
            ;;
            [test-app.home.view]
            [test-app.home.view-model]
            [test-app.submit.view]
            [test-app.submit.view-model]
            [test-app.menu.view]
            [test-app.menu.view-model])
  (:require-macros [cljs-log.core :as log]))

(venue/defview!
  {:target "app"
   :route "/"
   :id :views/home
   :view test-app.home.view/view
   :view-model test-app.home.view-model/handler
   :state {:text "Home Page"}})

(venue/defview!
  {:target "app"
   :route "/submit"
   :id :views/submit
   :view test-app.submit.view/view
   :view-model test-app.submit.view-model/handler
   :state {:text "Submit Page"}})

(venue/defstatic!
  {:target "menu"
   :id :static/menu
   :view test-app.menu.view/view
   :view-model test-app.menu.view-model/handler
   :state {}})

(venue/start!
```
This contrived example defines two views destined for the same target, based on different routes. It also defines a static view (always present, not subject to routing). Behind the curtain, venue will activate the appropriate view based on the current route. It will also send any events raised by that view to the applicable view-model for handling.


## Setup

To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

    lein clean

To create a production build run:

    lein cljsbuild once min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL.

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
