NOTE: Internally, Aesh uses multiple threads to process the stream and execute the queue of commands.  Due to the
multi-threaded environment, debugging these tests can be a bit of a pain.  Most notably, if a test pushes a
sequence of commands to the stream and you add a breakpoint on any of them, Aesh appears to continue
processing the sequence while you're stepping through the paused execution!  If nothing else, note that you may get
different test results when debugging!

Tip: try to comment-out all pushes to the stream, except for the one you're interested in.  As with any sort of
multi-threaded test environment, isolation is key!