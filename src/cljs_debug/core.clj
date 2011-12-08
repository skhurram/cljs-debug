(ns cljs-debug.core
  (:use [clojure.data.json :only [read-json]])
  (import [java.net URL]
          [se.cgbystrom.netty.http.websocket
           WebSocketCallback
           WebSocketClient
           WebSocketClientFactory]))

(read-json (slurp (URL. "http://localhost:9222/json")))
