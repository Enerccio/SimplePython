def anca():
    print "anca zacina"
    i = 0
    while True:
        if i == 99999:
            print "anca konci"
            return i
        i = i + 1
        
def test():
    print anca() + 10