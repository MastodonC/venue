(ns ^:figwheel-always venue.core
    (:require [cljs.core.async :refer [<! chan put! mult tap timeout pub sub unsub unsub-all]]
              [om.core :as om :include-macros true]
              [om-tools.dom :as dom :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [secretary.core :as secretary :refer-macros [defroute]])
    (:require-macros [cljs.core.async.macros :as am :refer [go go-loop alt!]]
                     [cljs-log.core :as log])
    (:import goog.History))

;; state blobs
(defonce venue-state (atom {}))
(defonce state (atom {:started? false
                      :service-loop? false
                      :services {}}))

;; channels
(defonce chan-sz 20)
(defonce refresh-ch (chan chan-sz))
(defonce refresh-mult (mult refresh-ch))
(defonce service-request-ch (chan chan-sz))
(defonce msgbus-publisher (chan chan-sz))
(defonce msgbus-publication (pub msgbus-publisher :topic))

;; other vars
(defonce history (History.))
(defonce log-prefix "[venue]")
(secretary/set-config! :prefix "#")

;; log prefix helpers
(defn log-debug  [& body] (log/debug  log-prefix (clojure.string/join " " body)))
(defn log-info   [& body] (log/info   log-prefix (clojure.string/join " " body)))
(defn log-warn   [& body] (log/warn   log-prefix (clojure.string/join " " body)))
(defn log-severe [& body] (log/severe log-prefix (clojure.string/join " " body)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defprotocol IHandleEvent
  (handle-event [this event args cursor]))

(defprotocol IHandleRequest
  (handle-request [this request args response-ch]))

(defprotocol IHandleResponse
  (handle-response [this outcome event response context]))

(defprotocol IActivate
  (activate [this args cursor]))

(defprotocol IInitialise
  (initialise [this cursor]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- fixture-by-id
  [id]
  (->> @venue-state
       (map val)
       (mapcat :fixtures)
       (map (partial apply hash-map))
       (reduce conj)
       id))

(defn- fixtures-by-target
  [target]
  (->> @venue-state target :fixtures))

(defn- route-list
  []
  (->> @venue-state
       (map val)
       (mapcat :fixtures)
       (map second)
       (map :route)
       set))

(defn- filter-fixtures
  [location opts]
  (let [{:keys [include-static?] :or {include-static? false}} opts]
    (->> @venue-state
         (map val)
         (mapcat :fixtures)
         (map second)
         (filter #(or (and include-static? (:static %)) (= (:route %) location))))))

(defn- get-current-fixtures
  []
  (->> @venue-state
       (map val)
       (map #(let [current (:current %)]
               (-> % :fixtures current)))))

(defn- fixture-by-cursor
  [cursor]
  (->> @venue-state
       (map val)
       (mapcat :fixtures)
       (map second)
       (filter #(= (:state %) @cursor))
       first))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn subscribe!
  [topic ch]
  (sub msgbus-publication topic ch))

(defn unsubscribe!
  [topic ch]
  (unsub msgbus-publication topic ch))

(defn publish!
  ([topic]
   (publish! topic nil))
  ([topic content]
   (let [payload {:topic topic :content content}]
     (log-debug "Message bus publish: " payload)
     (go (>! msgbus-publisher payload)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- init-services
  []
  (doseq [service (:services @state)]
    (let [handler ((val service))]
      (when (satisfies? IInitialise handler)
        (initialise handler nil)))))


(defn- install-om!
  [target target-element venue-cursor]
  (log-debug target " does not have an om/root. Installing now...")
  (let [event-chan (chan chan-sz)]
    (om/root
     (fn
       [cursor owner]
       (reify
         om/IWillUpdate
         (will-update [_ _ _])
         om/IWillMount
         (will-mount [_]
           (let [refresh-tap (tap refresh-mult (chan chan-sz))]
             ;; loop for refresh
             (go-loop []
               (let [_ (<! refresh-tap)]
                 (om/refresh! owner))
               (recur)))
           ;; FIXME there's something in this go block that upsets the compiler:
           ;; "WARNING: Use of undeclared Var venue.core/bit__16711__auto__"
           (go-loop []
             (let [{:keys [event args current-ids]} (<! event-chan)
                   {:keys [view-model id]} (some (fixtures-by-target target) current-ids)
                   vm ((view-model))]
               (when (satisfies? IHandleEvent vm)
                 (apply (partial handle-event vm) (conj [event args] (-> venue-cursor target :fixtures id :state)))))
             (recur)))
         om/IRender
         (render [_]
           (if-let [current-id (:current cursor)]
             (let [{:keys [view state]} (current-id (:fixtures cursor))]
               (dom/div nil
                        (if view
                          (om/build (view) state))))))))
     venue-state
     {:target target-element
      :path [target]
      :shared {:event-chan event-chan}}))
  (om/update! venue-cursor [target :installed?] true))

(defn- launch-route!
  ([location route-params]
   (launch-route! location route-params false))
  ([location route-params not-found?]
   (let [venue-cursor (om/root-cursor venue-state)]
     ;; loop routes applicable to this location
     (doseq [{:keys [target id]} (filter-fixtures (if not-found? :not-found location) {:include-static? true})]
       (if-let [target-element (.getElementById js/document (name target))]
         (do
           ;; if we're not installed, add an om/root
           (when-not (:installed? (get venue-cursor target))
             (install-om! target target-element venue-cursor))

           ;; init/activate vm
           (let [{:keys [view-model has-init?]} (fixture-by-id id)
                 vm ((view-model))
                 state (-> venue-cursor target :fixtures id :state)]
             (when-not has-init?
               (om/update! venue-cursor [target :fixtures id :has-init?] true)
               (when (satisfies? IInitialise vm)
                 (initialise vm state)))
             (when (satisfies? IActivate vm)
               (activate vm route-params state)))

           ;; write current id to state
           (om/update! venue-cursor [target :current] id))

         (log-warn target " couldn't be found. Use <TODO> to suppress the warning."))))))

(defn- add-view!
  [{:keys [target view view-model id state route] :as fix}]

  ;; FIXME perhaps we do something other than throw here?
  (if (fixture-by-id id)
    (throw (js/Error. (str "A route with id " id " already exists!"))))

  ;; check for new routes.
  (when-not (contains? (route-list) route)
    (log-debug "Defining a route for " route)
    (defroute (str route) {:as params}
      (log-info "Routing " route)
      (launch-route! route params)
      true))

  ;; save the view
  (let [ktarget (keyword target)
        mfix (-> fix
                 (assoc :target ktarget)
                 (assoc :static false))]
    (swap! venue-state assoc-in [ktarget :fixtures id] mfix)))

(defn- add-static-view!
  [{:keys [target id] :as fixture}]
  (let [ktarget (keyword target)]
    (swap! venue-state assoc-in [ktarget :fixtures id] (-> fixture
                                                           (assoc :target ktarget)
                                                           (assoc :static true)))))

(defn- start-service-loop!
  []
  (log/debug "Starting service loop.")
  (go-loop []
    (let [{:keys [owner
                  context
                  service
                  request
                  args
                  timeout-ms
                  timeout?]
           :or {timeout? true}} (<! service-request-ch)]
      (if-let [service-provider (get-in @state [:services service])]
        (let [c (chan)
              to (when timeout? (timeout (or timeout-ms 5000)))
              rfn (fn [owner outcome request data context]
                    (when (satisfies? IHandleResponse owner)
                      (handle-response owner outcome request data context)))
              alts-chans (remove nil? [c to])]
          (log-debug "Received service request:" service request)
          (go
            (let [[[outcome data] p] (alts! alts-chans)]
              (condp = p
                c (rfn owner outcome request data context)
                to (do
                     (log-warn "A service request timed out:" service request)
                     (rfn owner :failure request "The service request timed out" context)))))
          (handle-request (service-provider) request args c))
        (log-severe "A request was sent to an unknown service: " service)))
    (recur)))

(defn- add-service!
  [{:keys [id handler]}]
  (if-not handler
    (log-severe "Service is null." id)
    (do
      (log/debug "Adding service" id)
      (if (satisfies? IHandleRequest (handler))
        (swap! state assoc-in [:services id] handler)
        (log-severe "Services must implement IHandleRequest." id))

      (when-not (:service-loop? @state)
        (swap! state assoc :service-loop? true)
        (start-service-loop!)))))

(defn raise!
  ([owner event]
   (raise! owner event {}))
  ([owner event args]
   (let [c (om/get-shared owner [:event-chan])]
     (put! c {:event event :args args :current-ids (->> @venue-state
                                                        (map val)
                                                        (map :current))}))))

(defn request!
  [payload]
  (put! service-request-ch payload))

(defn get-route
  ([id]
   (get-route id {}))
  ([id opts]
   (if-let [route (->> id fixture-by-id :route)]
     (secretary/render-route route opts)
     (log-severe "get-route tried failed to find a fixture with the following id:" id))))

(defn navigate!
  ([id]
   (navigate! id {}))
  ([id opts]
   (let [route (get-route id opts)]
     (if (:static route)
       (throw (js/Error. "Cannot navigate to a static fixture!")))
     (log-info "Navigating to " route)
     (set! (.. js/document -location -href) route))))

(defn reactivate!
  []
  (secretary/dispatch! js/window.location.hash))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ROUTING FUNCTIONS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn on-navigate [event]
  (let [path (.-token event)]
    (when-not (secretary/dispatch! path)
      (log/info "View could not be found for this route:" path "- attempting to display 'not-found' view")
      (launch-route! path {} true))))

(defn set-up-history!
  []
  (doto history
    (goog.events/listen EventType/NAVIGATE on-navigate)
    (.setEnabled true)))

(defn on-js-reload []
  (log-debug "Refreshing views...")
  (put! refresh-ch true))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn start!
  []
  (when-not (:started? @state)
    (swap! state assoc :started? true)
    (log-info "Starting up...")
    (set-up-history!)
    (init-services)
    (launch-route! nil nil) ;; install statics
    (secretary/dispatch! js/window.location.hash)))
