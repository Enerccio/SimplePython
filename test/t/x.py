from co import coroutine
from threading.Thread import wait

def fnc():
    print "in"
    yield None
    print "doing job"
    last = None
    while True:
        next = yield last
        last = next
        wait(1000)

def test():
    x = coroutine(fnc)
    print x.status()
    print x.resume()
    print x.status()
    print x.resume("Ahoj Svet")
    print x.status()
    print x.resume("Test")
    print x.status()