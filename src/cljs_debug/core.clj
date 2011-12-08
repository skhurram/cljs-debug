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
    (println "message:" (.getTextData f)))
  (^void onError [this ^Throwable t]
    (.printStackStrace t)))

(defn ^DefaultWebSocketFrame frame [s]
  (DefaultWebSocketFrame. s))

(defn ^WebSocketClient make-client [uri]
  (let [f (WebSocketClientFactory.)
        cb (WSCallback.)]
    (.newClient f (URI. uri) cb)))

(comment
  (read-json (slurp (URL. "http://localhost:9222/json")))
  (def uri "ws://localhost:9222/devtools/page/8")
  (def c (make-client uri))
  (.connect c)
  (.disconnect c)

  (def rpc {"id" 0
            "method" "Runtime.evaluate"
            "params" {"expression" "cljs_conj.core.foo(5,5)"
                      "returnByValue" true}})

  ;; need to enable debugging first!
  (def dbg-enable {"id" 1
                   "method" "Debugger.enable"})

  (def dbg-eval {"id" 0
                 "method" "Runtime.evaluate"
                 "params" {"expression" "cljs_conj.core.foo(5,5)"
                           "returnByValue" true}})

  ;; seems to crash the WebSocket? - David
  (def bk1 {"id" 2
            "method" "Debugger.setBreakpoint"
            "params" {"location" {"lineNumber" 14393
                                  "scriptId" "22"}}})

  ;; these don't seem to work either - David
  (def pause {"id" 3
              "method" "Debugger.pause"})

  (def resume {"id" 4
              "method" "Debugger.resume"})

  (.send c (frame (json-str rpc)))
  (.send c (frame (json-str dbg-enable)))
  (.send c (frame (json-str bk1)))
  (.send c (frame (json-str pause)))
  (.send c (frame (json-str resume)))
  )
