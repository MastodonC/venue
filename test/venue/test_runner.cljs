(ns venue.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [venue.core-tests]))

 (doo-tests 'venue.core-tests)
