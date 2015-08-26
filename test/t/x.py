import threading

class Test(threading.Thread):
    def execute(self):
        while True:
            print "Test test"

t = Test(daemon=True)

def test2(type):
    print "In signal, if I throw exception here, however, it will propagate from signal back to original call!"
    if type is not None:
        raise type("die, thread!")

def test():
    print "Start"
    t.start()
    
    threading.Thread.wait(10)
    
    t.signal(test2, None)
    
    threading.Thread.wait(10)
    
    t.signal(test2, TypeError)
    
    t.join()
    print "Done"