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
    """
    Represents FIFO queue
    """
    def __init__(self):
        super(Queue, self).__init__()
        self._queue = []
        
    def add(self, object):
        """
        Adds object to queue
        """
        self._queue.append(object)
        
    def poll(self):
        """
        Returns value from the head of queue and removes that value from the queue
        """
        value = self._queue[0]
        self._queue = self._queue[1:]
        return value
    
    def has_elements(self):
        """
        Returns True if queue has elements
        """
        return len(self._queue) > 0
    
    def __str__(self):
        return "Queue=" + str(self._queue)
    
    def __len__(self):
        return len(self._queue)
        
class SynchronizedQueue(Queue):
    """
    Synchronized version of Queue.
    Uses Mutex to lock important structures
    """
    def __init__(self):
        super(SynchronizedQueue, self).__init__()
        self.mutex = Mutex()
        
    def add(self, object):
        """
        Adds object to queue
        """
        with self.mutex:
            Queue.add(self, object)
        
    def poll(self):
        """
        Returns value from the head of queue and removes that value from the queue
        """
        with self.mutex:
            return Queue.poll(self)
        
    def has_elements(self):
        """
        Returns True if queue has elements
        """
        with self.mutex:
            return Queue.has_elements(self)
    
    def __str__(self):
        with self.mutex:
            return "SynchronizedQueue" + str(self._queue)
            
    def __len__(self):
        with self.mutex:
            return Queue.__len__(self)
