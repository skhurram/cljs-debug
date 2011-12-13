(ns cljs-debug.core
  (:use [clojure.data.json :only [json-str read-json]]
        [clojure.pprint :only [pprint]])
  (:require [cljs.compiler :as cljsc])
  (import [java.net URL URI]
          [org.jboss.netty.handler.codec.http.websocket
           WebSocketFrame
           DefaultWebSocketFrame]
          [se.cgbystrom.netty.http.websocket
           WebSocketCallback
           WebSocketClient
           WebSocketClientFactory]))

;; =============================================================================
;; WebSockets

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

;; =============================================================================
;; Debugger

(def connections (atom {}))
(def dbg-state (atom {}))
(def dbg-db (atom {}))

(defn list-pages []
  (let [d (read-json (slurp (URL. "http://localhost:9222/json")))]
    (pprint (zipmap (range) (map :title d)))))

(defn connect-to! [n]
  (let [page (-> (URL. "http://localhost:9222/json")
                  slurp
                  read-json
                  (nth n))
        wsurl (:webSocketDebuggerUrl page)]
    (if wsurl
      (let [c (make-client wsurl)]
        (.connect c)
        (swap! connections (fn [m] (assoc m n c))))
      (println "Could not connect to:" (str "\"" (:title page) "\"")))))

(defn close-all! []
  (doseq [[id ^WebSocketClient c] connections]
    (.close c))
  (reset! connections {}))

(defn rpc [id msg]
  (let [^WebSocketClient c (get @connections id)]
    (.send c (frame (json-str msg)))
    nil))

(defn enable-debug! [id]
  (rpc id {"id" (rand-int 1000) "method" "Debugger.enable"}))

(defn instrument [file]
  )

(defn search [form]
  )

(defn set-debug-ns! [ns]
  )

(defn set-breakpoint! [ln]
  )

(defn unset-breakpoint! [ln]
  )

(defn step-in! [])

(defn step-out! [])

(defn step-over! [])

(defn locals []
  )

(defn dbg-eval [form]
  )

(comment
  (with-out-str 
    (cljsc/emit
     (cljsc/analyze {} '(defn foo [a b] (+ a b)))))

  (read-json (slurp (URL. "http://localhost:9222/json")))
  (def uri "ws://localhost:9222/devtools/page/3")
  (def c (make-client uri))
  (.connect c)
  (.disconnect c)

  (def simple-msg
    {"id" 0
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

  ;; whoa
  (def search {"id" 0
               "method" "Debugger.searchInContent"
               "params" {"scriptId" "22"
                         "query" "cljs_conj.core.foo"}})

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

  (def so {"id" 5
           "method" "Debugger.stepOver"})

  (.send c (frame (json-str rpc)))
  (.send c (frame (json-str dbg-enable)))
  (.send c (frame (json-str search)))
  (.send c (frame (json-str bk1)))
  (.send c (frame (json-str pause)))
  (.send c (frame (json-str resume)))
  (.send c (frame (json-str so)))

  ;; tomorrow add bar and set the breakpoint on bar

  ;; how can we map sexprs to line numbers? perhaps we can just do
  ;; function level mappings. then when we step through function forms
  ;; we can use the analyzer to get more accurate offsets.
  )
