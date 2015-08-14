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

import sys
from threading import *
from collections import SynchronizedQueue
from sync import Mutex

__all__ = ["EventReactor", "Event", "standard_events", "event_queue", "stop", "has_events"]

class __EventReactorThread(Thread):
    def __init__(self):
        self.stopped = False
        self.idle = 0
        super(__EventReactorThread, self).__init__(name="standard-eventreactor-thread", daemon=True)
    
    def execute(self):
        while not self.stopped:
            if not __std_er.poll():
                Thread.wait(self.idle)
            
    def stopEventThread(self):
        self.stopped = True

__std_er_t = __EventReactorThread()
__std_er = None
        
class Event(object):
    __event_counter = 0
    __event_mutex = Mutex()
    
    def __init__(self, callable, periodic=False, periodTime=0, initDelay=0, args=(), kwargs={}):
        self.callable = callable
        self.periodic = periodic
        self.periodTime = periodTime
        self.currentPTime = initDelay
        self.args = args
        if not type(args) == tuple:
            self.args = (self.args, )
        self.kwargs = kwargs
        
        __event_mutex.acquire()
        self.id = __event_counter
        __event_counter += 1
        __event_mutex.release()
        
    def call(self):
        self.callable(self, *self.args, **self.kwargs)
        
    def __str__(self):
        return ("Event [callable=" + str(self.callable) + ", periodic="+ str(self.periodic) + 
                ", pt=" + str(self.periodTime) + ", ct=" + str(self.currentPTime) + 
                ", args=" + str(self.args) + ", kwargs=" + str(self.kwargs) + "]")

class __QueueOperator(object):
    def __init__(self, queue):
        self.__queue = queue
        
    def enqueue(self, event, periodic=False, periodTime=0, initDelay=0, args=[], kwargs={}):
        if isinstance(event, Event):
            self.__queue.add(event)
            return event.id
        elif callable(event):
            event = Event(event, periodic, periodTime, initDelay, args, kwargs)
            self.__queue.add(event)
            return event.id
        else:
            raise TypeError("enqueue(): object is not of event type or callable")
        
    def __lshift__(self, event):
        return self.enqueue(event)
        
    def __rshift__(self, event_id):
        try:
            self.__queue.mutex.acquire()
            for event in self.__queue._queue:
                if event.id == event_id:
                    self.__queue._queue.remove(event)
                    return
        finally:
            self.__queue.mutex.release()

class EventReactor(object):
    def __init__(self):
        super(EventReactor, self).__init__()
        
        self.__event_queue = SynchronizedQueue()
        self.__enqueuer = __QueueOperator(self.__event_queue)
        
        self.__rshift__ = self.__enqueuer.__rshift__
        self.__lshift__ = self.__enqueuer.__lshift__
        
        self.__last_access_time = sys.current_time()
        
    def poll(self):
        dif = sys.current_time() - self.__last_access_time
        self.__last_access_time = sys.current_time() 
        try:
            self.__event_queue.mutex.acquire()
            if self.__event_queue.has_elements():
                event = self.__event_queue.poll()
                event.currentPTime -= dif
                if event.currentPTime <= 0:
                    event.currentPTime = event.periodTime
                    self.fire_event(event)
                    if event.periodic:
                        self.__enqueuer.enqueue(event)
                    return True
                else:
                    self.__event_queue.add(event)
            return False
        finally:
            self.__event_queue.mutex.release()
            
    def has_events(self):
        try:
            self.__event_queue.mutex.acquire()
            return len(self.__event_queue)
        finally:
            self.__event_queue.mutex.release()
            
    def fire_event(self, event):
        event.call()


_event_queue = None
_stop = None
_has_events = None

class __EventQueueProxy(object):
    def enqueue(self, event, periodic=False, periodTime=0, initDelay=0, args=[], kwargs={}):
        return _event_queue.enqueue(event, periodic, periodTime, initDelay, args, kwargs)
    
    def __lshift__(self, event):
        return _event_queue << event
        
    def __rshift__(self, event_id):
        return _event_queue >> event_id

class __StopProxy(object):
    def __call__(self):
        return _stop()
        
class __HasEventsProxy(object):
    def __call__(self):
        return _has_events()

event_queue = __EventQueueProxy()
stop = __StopProxy()
has_events = __HasEventsProxy()

def standard_events(poll_idle=10):
    global __std_er, _event_queue, _stop, _has_events
    if __std_er is not None:
        raise TypeError("standard_events can only be called once!")
    __std_er_t.idle = poll_idle
    __std_er = EventReactor(poll_idle)
    __std_er_t.start()
    _event_queue = __std_er
    _stop = __std_er_t.stopEventThread
    _has_events = __std_er.has_events
    
