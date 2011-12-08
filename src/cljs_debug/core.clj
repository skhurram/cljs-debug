(ns cljs-debug.core
  (:use [clojure.data.json :only [json-str read-json]])
  (import [java.net URL URI]
          [org.jboss.netty.handler.codec.http.websocket
           WebSocketFrame
           DefaultWebSocketFrame]
          [se.cgbystrom.netty.http.websocket
           WebSocketCallback
           WebSocketClient
           WebSocketClientFactory]))

(deftype WSCallback []
  WebSocketCallback
  (^void onConnect [this ^WebSocketClient c]
    (println "connected!"))
  (^void onDisconnect [this ^WebSocketClient c]
    (println "disconnected!"))
  (^void onMessage [this ^WebSocketClient c ^WebSocketFrame f]
    (println "message" (.getTextData f)))
  (^void onError [this ^Throwable t]
    (.printStackStrace t)))

(defn ^DefaultWebSocketFrame frame [s]
  (DefaultWebSocketFrame. s))

(defn ^WebSocketClient make-client []
  (let [f (WebSocketClientFactory.)
        cb (WSCallback.)]
    (.newClient f (URI. "ws://localhost:9222/devtools/page/1") cb)))

(comment
  (def rpc {"id" 0
            "method" "Runtime.evaluate"
            "params" {"expression" "4+5"
                      "returnByValue" true}})

  (def c (make-client))
  (.connect c)
  (.send c (frame (json-str rpc)))
  (.disconnect c)
  (read-json (slurp (URL. "http://localhost:9222/json")))
  )
