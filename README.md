cljs-debug
===

A debugging infrastructure for ClojureScript that leverages the WebKit Remote Debugging
Protocol as featured in nightly builds of Chromium and WebKit.

Getting Started
---

Install Leiningen. In your checkout run <code>lein deps</code>.

Then compile the simple ClojureScript example that comes with the repo at the command
line by starting a Leiningen REPL with <code>lein repl</code>. At the Clojure REPL
enter the following:

```console
user=> (require '[cljs.closure :as cljsc])
user=> (cljsc/build "example/" {:optimizations :simple :pretty-print true :output-to "example/main.js"})
```

In order to debug the generated ClojureScript you will need a nightly build of Chromium.
You can get that [here](http://commondatastorage.googleapis.com/chromium-browser-continuous/index.html)

Start the browser at the command line:

```console
/full/path/to/Chromium --remote-debugging-port=9222
```

Now we can connect to this page to debug it. Start a Leiningen REPL if you don't already
have one running with <code>lein repl</code>

```console
user=> (use 'cljs-debug.core)
user=> (list-pages)
...
user=> (connect-to! 1)
...
user=> (debug-enable! 1)
...
```

License
---

Copyright (C) 2011 David Nolen

Distributed under the Eclipse Public License, the same as Clojure.
