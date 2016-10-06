(ns utility-works.async-test
  (:require [utility-works.async :refer :all]
            [clojure.test :refer :all]
            [clojure.core.async :as a]))

(deftest batch-process-test
  (testing "processes messages according to batch size and timeout"
    (let [result (atom [])
          timing (atom nil)
          start-time (System/currentTimeMillis)
          messages (a/chan 100)]
      (batch-process messages
                     (partial swap! result conj)
                     4
                     100
                     (fn []
                       (reset! timing
                               (- (System/currentTimeMillis)
                                  start-time))))

      (a/onto-chan messages (range 10) false)
      (Thread/sleep 120)
      (a/>!! messages 10)
      (Thread/sleep 120)
      (a/onto-chan messages (range 10) false)
      (Thread/sleep 240)
      (a/>!! messages 10)

      (a/close! messages)

      (is (= [[0 1 2 3]
              [4 5 6 7]
              [8 9]
              [10]
              [0 1 2 3]
              [4 5 6 7]
              [8 9]
              [10]]
             @result))

      (testing "calls the wrapup-f afterwards"
        (is (< 480 @timing))))))
