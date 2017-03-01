(ns rekry.middleware)

(defn foo-response [response value]
  (assoc-in response [:headers "X-Foo"] value))

(defn wrap-foo [handler value]
  (fn
    ([request]
     (foo-response (handler request) value))
    ([request respond raise]
     (handler request #(respond (foo-response % value)) raise))))
