(ns utility-works.async
  (:require [clojure.core.async :as a]))

(defn batch-process [ch f batch-size timeout]
  "Take from ch and call f on a seq of messages every batch-size
  messages or every timeout milliseconds, whichever comes first."
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
            (when-not (nil? message)
              (recur timeout-ch
                     (conj messages message))))
        timeout-ch (do
                     (when (seq messages)
                       (a/thread
                         (f messages)))
                     (recur (a/timeout timeout)
                            []))))))
