class X(object):
    def __larrow__(self, x):
        print self, x

def test():
    x = X()
    y = 1
    x<-y