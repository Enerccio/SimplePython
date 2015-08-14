"""
SimplePython - embeddable python interpret in java
Copyright (c) Peter Vanusanik, All rights reserved.
 
eventlib module
 
This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3.0 of the License, or (at your option) any later version.
 
This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.
 
You should have received a copy of the GNU Lesser General Public
License along with this library.
"""

from threading import *
from collections import SynchronizedQueue

__all__ = ["EventReactor", "standard_events"]

class __EventReactorThread(Thread):
    def __init__(self):
        super(__EventReactorThread, self).__init__(name="standard-eventreactor-thread")
    
    def execute(self):
        __std_er.poll()

__std_er_t = __EventReactorThread()
__std_er = None

class EventReactor(object):
    def __init__(self, poll_idle=10):
        super(EventReactor, self).__init__()
        
        self.__poll_idle = poll_idle
        self.__event_queue = SynchronizedQueue()
        
    def poll(self):
        pass

def standard_events(poll_idle=10):
    global __std_er
    if __std_er is not None:
        raise TypeError("standard_events can only be called once!")
    __std_er = EventReactor(poll_idle)
    __std_er_t.start()