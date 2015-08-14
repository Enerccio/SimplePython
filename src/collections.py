"""
SimplePython - embeddable python interpret in java
Copyright (c) Peter Vanusanik, All rights reserved.
 
collections module
 
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

from sync import Mutex

__all__ = ["Queue", "SynchronizedQueue"]

class Queue(object):
    def __init__(self):
        super(Queue, self).__init__()
        self._queue = []
        
    def add(self, object):
        self._queue.append(object)
        
    def poll(self):
        value = self._queue[0]
        self._queue = self._queue[1:]
        return value
    
    def has_elements(self):
        return len(self._queue) > 0
    
    def __str__(self):
        return "Queue=" + str(self._queue)
    
    def __len__(self):
        return len(self._queue)
        
class SynchronizedQueue(Queue):
    def __init__(self):
        super(SynchronizedQueue, self).__init__()
        self.mutex = Mutex()
        
    def add(self, object):
        self.mutex.acquire()
        try:
            Queue.add(self, object)
        finally:
            self.mutex.release()
        
    def poll(self):
        self.mutex.acquire()
        try:
            return Queue.poll(self)
        finally:
            self.mutex.release()
        
    def has_elements(self):
        self.mutex.acquire()
        try:
            return Queue.has_elements(self)
        finally:
            self.mutex.release()
    
    def __str__(self):
        self.mutex.acquire()
        try:
            return "SynchronizedQueue" + str(self._queue)
        finally:
            self.mutex.release()
            
    def __len__(self):
        self.mutex.acquire()
        try:
            return Queue.__len__(self)
        finally:
            self.mutex.release()
