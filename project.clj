(defproject advent "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [digest "1.4.4"]
                 [org.clojure/math.combinatorics "0.1.1"]
                 [org.clojure/core.match "0.2.2"]]

  
  :profiles { :dev { :dependencies [[org.clojure/tools.nrepl "0.2.12"]]}
              :repl { :plugins [[cider/cider-nrepl "0.10.0-SNAPSHOT"]]
                      :repl-options {:init (set! *print-length* 500)}} })
