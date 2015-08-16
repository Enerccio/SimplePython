class X(object):
    def __init__(self):
        self.ec = 0
    def __enter__(self):
        try:
            return self.ec
        finally:
            self.ec += 1
    
    def __exit__(self, type, value):
        print type, value
        
def test():
    x = X()
    for a in (0, 1):
        try:
            with x as y:
                print y
                break
        finally:
            print "b"
        print "a"