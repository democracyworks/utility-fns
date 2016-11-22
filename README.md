# utility-fns

Clojure functions that are general-purpose enough to be useful
elsewhere but haven't found a home in a nice library yet.

## core.async

### `batch-process`

`batch-process` takes from a channel and calls a function on sequences
of messages from that channel. It is triggered every time the channel
provides a batch of the given size, or every time the timeout elapses,
whichever comes first.

```clj
(batch-process ch prn
               {:batch-size 4
                :timeout 1000
                :wrapup-f #(println "Done!")})
(async/onto-chan ch (range 10) false)
(Thread/sleep 2000)
(async/>!! ch 10)
```

That will end up printing:

```
[0 1 2 3]
[4 5 6 7]
[8 9]
[10]
Done!
```

The options `batch-process` takes are:

* `:batch-size`: The number of messages required to trigger a call of
  the processing function. Default: 100.
* `:timeout`: The number of milliseconds to trigge a call of the
  processing function even if the number of messages hasn't reached
  the batch size. Default: 5000.
* `:wrapup-f`: A 0-arity function to call once the channel is closed
  and jobs have completed.
* `:pool-size`: The number of threads to use for processing
  messages. Work will block when all threads are occupied. If nil,
  will use core.async's thread pool, which is unbounded. Default: nil.

## License

Copyright © 2016 Democracy Works, Inc.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
