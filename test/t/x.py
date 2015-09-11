
def call_me(x):
    return 1;

def fnc():
    v = 0
    for i in xrange(1280):
        for j in xrange(1280):
            v += call_me(i+j)
    return v

def test():
    print fnc()