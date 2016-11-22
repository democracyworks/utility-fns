(defproject democracyworks/utility-fns "0.2.0-SNAPSHOT"
  :description "Clojure functions that are general-purpose enough to be useful elsewhere but haven't found a home in a nice library yet."
  :url "https://github.com/democracyworks/utility-fns"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.2.391"]]
  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]]
  :source-paths ["src/clojure"]
  :java-source-paths ["src/java"])
