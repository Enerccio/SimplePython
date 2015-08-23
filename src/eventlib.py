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

__all__ = ["EventReactor", "ThreadedEventReactor", "Event", "standard_events", "event_queue", "stop", "has_events"]
        
class Event(object):
    """
    Base Event class. Provides standard handling of events along with timed events and refireable events
    """
    __event_counter = 0
    __event_mutex = Mutex()
    
    def __init__(self, callable, periodic=False, periodTime=0, initDelay=0, args=(), kwargs={}):
        """
        Creates event out of callable. periodic value represents whether this event will autoqueue 
        after it has been fired, periodTime is how long it will take after firing of the event to
        queue again, initDelay is initial delay before event is fired for the fist time and 
        args, kwargs are values used with the callable
        """
        self.callable = callable
        self.periodic = periodic
        self.periodTime = periodTime
        self.currentPTime = initDelay
        self.args = args
        if not type(args) == tuple:
            self.args = (self.args, )
        self.kwargs = kwargs
        
        with __event_mutex:
            self.id = __event_counter
            __event_counter += 1
        
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
        return self.remove(event_id)
    
    def remove(self, id_or_event):
        with self.__queue.mutex:
            if id_or_event in self.__queue:
                return self.__queue.remove(id_or_event)
            for event in self.__queue._queue:
                if event.id == event_id:
                    return self.__queue._queue.remove(event)

class EventReactor(object):
    """
    EventReactor is base class for all EventReactors.
    Provides a way to queue new events and then EventReactor deals with
    the events
    """
    
    def __init__(self):
        """
        Creates new EventReactor with standard SynchronizedQueue and __QueueOperator
        """
        self.__event_queue = SynchronizedQueue()
        self.__enqueuer = __QueueOperator(self.__event_queue)
        
        self.__rshift__ = self.__enqueuer.__rshift__
        self.__lshift__ = self.__enqueuer.__lshift__
        
        self.__last_access_time = sys.current_time()
        
    def poll(self):
        """
        When called, one pending event will be resolved.
        Returns True if at least one event was resolved or False if queue is empty
        """
        dif = sys.current_time() - self.__last_access_time
        self.__last_access_time = sys.current_time() 
        with self.__event_queue.mutex:
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
            
    def has_events(self):
        """
        Returns True if this reactor has events enqueued. 
        This might not be accurate value due to threading issues
        """
        with self.__event_queue.mutex:
            return len(self.__event_queue)
            
    def fire_event(self, event):
        """
        Base method, calls .call() on event. Is called when event needs to be fired by poll method
        """
        event.call()
        
class ThreadedEventReactor(EventReactor, Thread):
    """
    Threaded version of EventReactor.
    This EventReactor looks like a thread and when started will poll events until stopped.
    Provided idle_time is waited if reactor has no events in queue
    """
    
    def __init__(self, idle_time=10):
        self.stopped = False
        self.idle = idle_time
        EventReactor.__init__(self)
        Thread.__init__(self, name="standard-eventreactor-thread", daemon=True)
    
    def execute(self):
        while not self.stopped:
            if not self.poll():
                Thread.wait(self.idle)
            
    def stop_event_thread(self):
        """
        Stops this event reactor. Cannot be restarted
        """
        self.stopped = True

_event_queue = None
_stop = None
_has_events = None

class __EventQueueProxy(object):
    def enqueue(self, event, periodic=False, periodTime=0, initDelay=0, args=[], kwargs={}):
        return _event_queue.enqueue(event, periodic, periodTime, initDelay, args, kwargs)
    
    def __lshift__(self, event):
        return _event_queue.__lshift__(event)
        
    def __rshift__(self, event_id):
        return _event_queue.__rshift__(event_id)

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
    """
    standard_events creates the necessary structures for standard events to work with global functions.
    Creates new thread for the event queue and move the proxies so all works correctly.
    """
    global _event_queue, _stop, _has_events
    if _event_queue is not None:
        raise TypeError("standard_events can only be called once!")
    _event_queue = ThreadedEventReactor(poll_idle)
    _stop = _event_queue.stop_event_thread
    _has_events = _event_queue.has_events
    _event_queue.start()
    
