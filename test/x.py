import eventlib

count = 0
def xxx(event, arg):
    global count
    print "event happened!!! ", str(event), arg
    count += 1
    if count > 5:
        event.periodic = False

def test():
    eventlib.standard_events()
    id = eventlib.event_queue << eventlib.Event(xxx, True, 1000, args="test")
    print "event id", id
    while eventlib.has_events():
        pass