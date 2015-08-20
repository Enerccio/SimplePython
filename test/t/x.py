def p():
    def x():
        a = 5
        b = "test"
        print environment()
        print environment().__class__
        print dir(environment())
        print list(environment())
        print len(environment())
        print environment().resolve_local("a")
        print environment().resolve_nonlocal("x")
        print frame(0).get_environment().resolve_global("environment")
        print frame(0).get_environment().resolve_local("b")
        frame(0).get_environment()[0]["c"] = "TEEEBURU"
        print frame(0).get_environment().resolve_local("c")
        try:
            print frame(0).get_environment().resolve_local("d")
        except:
            pass
        frame(0).get_environment()[0][10] = "XD"
        
    return x

def test():
    p()()