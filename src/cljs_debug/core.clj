(ns cljs-debug.core
  (:use [clojure.data.json :only [read-json]])
  (import [java.net URL]))

(read-json (slurp (URL. "http://localhost:9222/json")))
