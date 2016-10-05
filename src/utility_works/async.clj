(ns utility-works.async
  (:require [clojure.core.async :as a]))

(defn batch-process
  "Take from ch and call f on a seq of messages every batch-size
  messages or every timeout milliseconds, whichever comes first. An
  optional wrapup-f will be called after ch has been closed and
  emptied."
  ([ch f batch-size timeout]
   (batch-process ch f batch-size timeout (constantly nil)))
  ([ch f batch-size timeout wrapup-f]
   (a/go-loop [timeout-ch (a/timeout timeout)
               messages []]
     (if (= batch-size (count messages))
       (do
         (a/thread
           (f messages))
         (recur (a/timeout timeout)
                []))
       (a/alt!
         ch ([message]
             (if (nil? message)
               (wrapup-f)
               (recur timeout-ch
                      (conj messages message))))
         timeout-ch (do
                      (when (seq messages)
                        (a/thread
                          (f messages)))
                      (recur (a/timeout timeout)
                             [])))))))
