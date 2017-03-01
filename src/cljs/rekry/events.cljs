(ns rekry.events
    (:require [re-frame.core :as re-frame]
              [rekry.db :as db]
              [day8.re-frame.http-fx]
              [ajax.core :as ajax]))


(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))

(re-frame/reg-event-db
 :set-active-panel
 (fn [db [_ active-panel]]
   (print "hei")
   (if (:nick db)
     (assoc db :active-panel active-panel)
     (assoc db :active-panel :choose-username-panel))))

(re-frame/reg-event-fx
 :set-nick
 (fn [world [_ nick]]
   {:db (assoc (:db world) :nick (:nick-sugg (:db world)))
    :dispatch-n [[:set-active-panel :chat-panel] [:poll-msgs]]}))

(re-frame/reg-event-db
 :available-nick-res
 (fn [db [_ res]]
   (let [msg (cond (:available res) "Nickname is available :)"
                   (not (:available res)) "Nickname is not available :(")]
     (-> db
         (assoc :nick-info-text msg)
         (assoc :nick-available (:available res))))))

(re-frame/reg-event-db
 :bad-http-res
 (fn [db [_ res]]
   (print res)
   db))

(defn valid? [nick]
  (cond (= 0 (count nick)) false
        :else true))

(re-frame/reg-event-db
 :invalid-nick
 (fn [db [_ nick]]
   (-> db
       (assoc :nick-info-text "Nickname is not valid. Nickname can only contain letters and numbers, and cannot be empty.")
       (assoc :nick-available false))))

(re-frame/reg-event-db
 :message-changed
 (fn [db [_ msg]]
   (assoc db :message msg)))

(re-frame/reg-event-db
 :channel-joined
 (fn [db [_ channel]]
   (assoc db :joined-channels (conj (:joined-channels db) channel))))

(re-frame/reg-event-db
 :channel-clicked
 (fn [db [_ channel]]
   (assoc db :chosen-channel channel)))

(re-frame/reg-event-fx
 :nick-changed
 (fn [world [_ nick]]
   (if (valid? nick)
     {:http-xhrio {:method     :get
                   :uri        (str "/available/" nick)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [:available-nick-res]
                   :on-failure   [:bad-http-res]}
      :db         (assoc (:db world) :nick-sugg nick)}
     {:dispatch   [:invalid-nick nick]})))

(defn max-id [vec]
  (apply max (map #(:id %) vec)))

(re-frame/reg-event-fx
 :poll-success
 (fn [world [_ res]]
   (print res)
   {:db (-> (:db world)
            (assoc :max-id (max-id res))
            (assoc :messages (concat res (:messages (:db world)))))
    :dispatch [:poll-msgs]}))

(re-frame/reg-event-fx
 :poll-msgs
 (fn [world _]
   (print world)
   {:http-xhrio {:method :get
                 :uri    (str "/poll-msgs" "?" (:max-id (:db world)))
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success  [:poll-success]
                 :on-failure     [:poll-msgs]}}))

(re-frame/reg-event-fx
 :send-message
 (fn [world [_ msg]]
   (print "asded")
   {:http-xhrio {:method :post
                 :uri    "/new-msg"
                 :params {:message (:message (:db world))
                          :author (:nick (:db world))
                          :channel (:chosen-channel (:db world))}
                 :format (ajax/json-request-format)
                 :response-format :text
                 :on-success  [:bad-http-res]
                 :on-failure     [:bad-http-res]}}))

(re-frame/reg-event-fx
 :success-messages
 (fn [world [_ res]]
   (print res)
   {
    :db (assoc (:db world) :messages res)}))

(re-frame/reg-event-fx
 :get-messages
 (fn [world _]
   {:http-xhrio {:method     :get
                :uri         (str "/messages/" (get-in world [:db :nick]))
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success [:success-messages]
                :on-failure   [:bad-http-res]}}))
