from co import coroutine
from threading.Thread import wait

def fnc():   
    yield None 
    
    last = "first"
    while True:
        wait(1000)
        next = yield last
        last = next

def test():
    x = coroutine(fnc)
    print x.resume()
    print x.resume("Ahoj Svet")
    x.terminate()