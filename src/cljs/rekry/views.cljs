(ns rekry.views
    (:require [re-frame.core :as re-frame]))


(defn home-panel []
  (let [name (re-frame/subscribe [:name])]
    (fn []
      [:div (str "Hello "@name ". This is the Home Page.")
       [:div [:a {:href "#/about"} "go to About Page"]]])))


;; about

(defn about-panel []
  (fn []
    [:div "This is the About Page."
     [:div [:a {:href "#/"} "go to Home Page"]]]))

(defn choose-username-panel []
  (let [available (re-frame/subscribe [:nick-available])
        nick-info-text (re-frame/subscribe [:nick-info-text])]
    (fn []
      [:div "Enter a nickname and click join!"
       [:div
        [:input {:on-change #(re-frame/dispatch  [:nick-changed (-> % .-target .-value)])}]
        [:input (merge {:type "submit"
                        :value "Join"
                        :on-click #(re-frame/dispatch  [:set-nick])}
                       (when (not @available)
                         {:disabled "disabled"}))]]
       [:div @nick-info-text]])))

(defn channels-component []
  (let [channels (re-frame/subscribe [:channels])]
    (fn []
      [:div
       (for [channel @channels]
         [:input {:on-click #(re-frame/dispatch [:channel-clicked channel])
                  :value (str channel)
                  :type "submit"}])])))

(defn send-message-component []
  (let [message (re-frame/subscribe [:message])
        nick-info-text (re-frame/subscribe [:nick-info-text])]
    (fn []
      [:div
       [:input {:on-change #(re-frame/dispatch  [:message-changed (-> % .-target .-value)])
                :value @message}]
       [:input (merge {:type "submit"
                       :value "Send message"
                       :on-click #(re-frame/dispatch  [:send-message])}
                      (when (empty? @message)
                        {:disabled "disabled"}))]])))

(defn messages-component []
  (let [msgs (re-frame/subscribe [:messages])
        channel (re-frame/subscribe [:chosen-channel])]
    (fn []
      [:div [:h4 @channel]
       (doall
        (for [msg (sort-by :id @msgs)]
          (if (= (:channel msg) @channel)
            [:p [:b (str (:author msg) ": ")] (:message msg)])))])))

(defn channel-component []
  [:div
   [messages-component]
   [send-message-component]])

(defn chat-panel []
  (let [nick (re-frame/subscribe [:nick])
        messages (re-frame/subscribe [:messages])]
    (fn []
      [:div (str "Hello " @nick ". Choose a channel below and start chatting!")
       [channels-component]
       [channel-component]])))

;; main

(defn- panels [panel-name]
  (case panel-name
    :choose-username-panel [choose-username-panel]
    :about-panel [about-panel]
    :chat-panel [chat-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [:active-panel])]
    (fn []
      [show-panel @active-panel])))
