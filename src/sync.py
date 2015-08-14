"""
SimplePython - embeddable python interpret in java
Copyright (c) Peter Vanusanik, All rights reserved.
 
sync module
 
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

NANOSECONDS = "NANOSECONDS"
MICROSECONDS = "MICROSECONDS"
MILLISECONDS = "MILLISECONDS"
SECONDS = "SECONDS"
MINUTES = "MINUTES" 
HOURS = "HOURS"
DAYS = "DAYS"

__all__ = (["Mutex"] + 
    [NANOSECONDS, MICROSECONDS, MILLISECONDS, SECONDS, MINUTES, HOURS, DAYS])

class Mutex(object):
    def __init__(self):
        super(Mutex, self).__init__()
        self.__jmutex = javainstance("jmutex")
        
    def acquire(self, timeout=None, units=MILLISECONDS):
        if timeout is None:
            self.__jmutex.acquire()
        else:
            self.__jmutex.try_acquire_timeout(timeout, units)
        
    def try_acquire(self):
        self.__jmutex.try_acquire()
        
    def release(self):
        self.__jmutex.release()
        
    def __enter__(self):
        self.acquire()
        return self
    
    def __exit__(self, t, v):
        self.release()