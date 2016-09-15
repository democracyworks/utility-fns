# utility-works

A Clojure library of utility functions.

## core.async

### `batch-process`

`batch-process` takes from a channel and calls a function on sequences
of messages from that channel. It is triggered every time the channel
provides a batch of the given size, or every time the timeout elapses,
whichever comes first.

```clj
(batch-process ch prn 4 1000)
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
```

## License

Copyright © 2016 Democracy Works, Inc.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
