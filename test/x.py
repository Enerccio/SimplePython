def x():
    c = 0
    for a in xrange(1024):
        for b in xrange(1024):
            c += a + b
    return c 

def test():
    print x()