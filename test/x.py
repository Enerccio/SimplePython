def anca():
    print "anca zacina"
    for i in xrange(0, 10000):
        if i == 9999:
            print "anca konci"
            return i
        
def test():
    print anca() + 10