(ns utility-fns.async-test
  (:require [utility-fns.async :refer :all]
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
                     {:batch-size 4
                      :timeout 100
                      :wrapup-f (fn []
                                  (reset! timing
                                          (- (System/currentTimeMillis)
                                             start-time)))})

      (a/onto-chan messages (range 10) false)
      (Thread/sleep 120)
      (a/>!! messages 10)
      (Thread/sleep 120)
      (a/onto-chan messages (range 10) false)
      (Thread/sleep 240)
      (a/>!! messages 10)

      (a/close! messages)
      (Thread/sleep 20)

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
        (is (< 480 @timing)))))

  (testing "with a bounded thread pool"
    (let [result (atom #{})
          timing (atom nil)
          start-time (System/currentTimeMillis)
          messages (a/chan 100)

          processing-ch
          (batch-process messages
                         (fn [messages]
                           (Thread/sleep 250)
                           (swap! result conj messages))
                         {:batch-size 4
                          :timeout 100
                          :pool-size 2
                          :wrapup-f (fn []
                                      (reset! timing
                                              (- (System/currentTimeMillis)
                                                 start-time)))})]

      (a/onto-chan messages (range 17))

      ;; Wait for the work to finish
      (a/<!! processing-ch)

      (is (= #{[0 1 2 3]
               [4 5 6 7]
               [8 9 10 11]
               [12 13 14 15]
               [16]}
             @result))

      (testing "calls the wrapup-f after all the work is done"
        (is (< 750 @timing 800))))))
