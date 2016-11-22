(ns utility-fns.async
  (:require [clojure.core.async :as a])
  (:import [utility_fns.async BoundedExecutor]
           [java.util.concurrent TimeUnit]))

(def batch-process-defaults
  {:batch-size 100
   :timeout 5000
   :pool-size nil
   :wrapup-f (constantly nil)})

(defn batch-process*
  [ch f {:keys [batch-size timeout wrapup-f]}]
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
              (do
                (when (seq messages)
                  (f messages))
                (wrapup-f))
              (recur timeout-ch
                     (conj messages message))))
        timeout-ch (do
                     (when (seq messages)
                       (a/thread
                         (f messages)))
                     (recur (a/timeout timeout)
                            []))))))

(defn bounded-batch-process*
  [ch f {:keys [batch-size timeout pool-size wrapup-f]}]
  (a/thread
    (let [executor (BoundedExecutor. pool-size)]
      (loop [timeout-ch (a/timeout timeout)
             messages []]
        (if (= batch-size (count messages))
          (do
            (.blockingSubmit executor
                             #(f messages))
            (recur (a/timeout timeout)
                   []))
          (a/alt!!
            ch ([message]
                (if (nil? message)
                  (do
                    (when (seq messages)
                      (.blockingSubmit executor
                                       #(f messages)))
                    (.shutdown executor)
                    (.awaitTermination executor
                                       (* 10 timeout)
                                       TimeUnit/MILLISECONDS)
                    (wrapup-f))
                  (recur timeout-ch
                         (conj messages message))))
            timeout-ch (do
                         (when (seq messages)
                           (.blockingSubmit executor
                                            #(f messages)))
                         (recur (a/timeout timeout)
                                []))))))))

(defn batch-process
  "Repatedly take from ch and call f on a seq of messages whenever a
  threshold of messages has been collected or a timeout has been
  reached, whichever comes first.

  Options:
  :batch-size  The number of messages to call the processing function on.
               Default 100.
  :timeout     The number of milliseconds to call the processing
               function even if the batch-size has not been reached.
               Default 5000.
  :pool-size   The number of threads to use for processing. If nil,
               will use core.async's thread pool, which is unbounded.
               Default nil.
  :wrapup-f    A 0-arity function to call once the channel has been closed
               and all jobs have been submitted. Default (constantly nil)"
  ([ch f]
   (batch-process ch f batch-process-defaults))
  ([ch f options]
   (let [{:keys [pool-size] :as options}
         (merge batch-process-defaults options)]
     (if pool-size
       (bounded-batch-process* ch f options)
       (batch-process* ch f options)))))
