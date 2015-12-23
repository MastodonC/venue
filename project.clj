(defproject venue "0.1.9"
  :description "Experimental MVVM-like framework for ClojureScript"
  :url "https://github.com/mastodonc/venue"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :lein-release {:deploy-via :clojars}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "0.0-3297"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.omcljs/om "0.9.0"]
                 [prismatic/om-tools "0.4.0" ]
                 [secretary "1.2.3"]
                 [cljs-log "0.2.2"]]

  :plugins [[lein-cljsbuild "1.1.0"]
            [lein-doo "0.1.5-SNAPSHOT"]]

  :deploy-repositories [["releases" :clojars]]

  :cljsbuild {:builds {:test {:source-paths ["src/" "test/"]
                              :compiler {:output-to "resources/public/js/testable.js"
                                         :main 'venue.test-runner
                                         :optimizations :none}}}})
