
def call_me(x):
    if (x % 2) == 0:
        return 1
    return 0

def fnc():
    v = 0
    for i in xrange(1280):
        for j in xrange(1280):
            v += call_me(i^j)
    return v

def test():
    print fnc()