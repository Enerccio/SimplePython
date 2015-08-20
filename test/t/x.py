import dis

def p():
    def x():
        a = 5
        b = "test"
        c = x + (future: p())
        print environment()
        print a, b
        print environment()[0]
        dis.dis(frame(0).get_bound_code())
        
    return x

def test():
    p()()