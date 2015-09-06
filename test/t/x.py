from co import coroutine
from threading.Thread import wait

def fnc():   
    yield None 
    
    last = "first"
    while True:
        wait(1000)
        next = yield last
        last = next
        
class t3(object):
    def __init__(self):
        t2()

def t2():
    x = coroutine(fnc)
    print x.resume()
    print x.resume("Ahoj Svet")
    x.terminate()
    
def test():
    x = 5
    t1(x)
    
def t1(y):
    t3()