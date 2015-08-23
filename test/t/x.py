import hw

def test():
    print hw.value
    hw.value = 5
    print hw.do_something(hw.value)