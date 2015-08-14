import eventlib
import threading
import collections

def test():
    x = collections.SynchronizedQueue()
    print x
    x.add(10)
    x.add(20)
    print x
    print x.peek()
    print x.poll()
    print x.poll()
    print x.poll()