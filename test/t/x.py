class Y(object):
    def __init__(self):
        print frame()
        print frame(0)
        print frame(0).get_local_context()

def x():
    print frame()
    print dir(frame(0))
    print frame(0).get_environment()
    print frame(0).get_local_context()
    Y() 

def test():
    x()