
class X:
    def __str__(self):
        raise "X - " + str(TypeError(self))


def test():
    print X()