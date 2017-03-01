(ns rekry.handler
  (:require [compojure.core :refer [GET POST defroutes]]
            [compojure.route :refer [resources]]
            [ring.core.protocols :refer :all]
            [ring.util.response :refer [resource-response response]]
            [hiccup.page :refer [include-js include-css html5]]
            [clojure.core.async :as a :refer [>! <! >!! <!! go close!]]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [org.httpkit.client :as http]
            [config.core :refer [env]]
            [manifold.deferred :as d]
            [manifold.stream :as s]
            [rekry.middleware :refer :all]
            ))

(defonce msgs-out (s/stream))

(def clients
  (atom {})) 

(let [max-id (atom 0)]
  (defn next-id []
    (swap! max-id inc)))

(defonce all-msgs (ref [{:id (next-id)
                         :message "this is a live chatroom, have fun",
                         :author "system"
                         :channel "Main"}]))

(defn get-msgs [max-id]
  (filter #(> (-> %1 :id) max-id) @all-msgs))

(defn new-msg [req res raise]
  (let [{:keys [message author channel]} (json/read-json (slurp (:body req)))
        data {:message message :author author :channel channel :id (next-id)}]
    (dosync (alter all-msgs conj data))
    (doseq [s (keys @clients)]
      (s/put! s data))
    (res {:status 200 :headers {}})))

(defn s-handler
  [req res raise]
  (let [id (Integer/valueOf (:query-string req))
        msgs (get-msgs id)]
    (if (seq msgs)
      (res {:status  200 :body (json/json-str msgs)})
      (let [stream (s/stream)]
        (swap! clients assoc stream id)
        (d/let-flow [msg (s/take! stream)]
          (res {:status 200 :body (json/write-str [msg])}))))))


(defroutes routes
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (GET "/messages/:nick" [nick] (response (json/write-str [{:author "peter"
                                                            :message "hei"
                                                            :channel "Main"}])))
  (GET "/available/:nick" [nick] (response (json/write-str {:available true})))
  (GET "/poll-msgs" [] s-handler)
  (POST "/new-msg" [] new-msg)
  (resources "/"))


(def handler #'routes)
