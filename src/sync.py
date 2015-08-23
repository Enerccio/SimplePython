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
    """
    Represents Lock mutex object.
    Provides methods related to locking and unlocking underline java mutex. 
    Can be used with with statement
    """
    def __init__(self):
        super(Mutex, self).__init__()
        self.__jmutex = javainstance("__jmutex__")
        
    def acquire(self, timeout=None, units=MILLISECONDS):
        """
        Acquires this mutex. Blocks until mutex is acquired or if 
        timeout is not None, waits that many units and either acquires the mutex 
        or fails.
        Returns True if mutex was acquired, will always be True if no timeout is used
        """
        if timeout is None:
            self.__jmutex.acquire()
            return True
        else:
            return self.__jmutex.try_acquire_timeout(timeout, units)
        
    def try_acquire(self):
        """
        Tries to acquire the mutex. Returns whether it was acquired or not
        """
        return self.__jmutex.try_acquire()
        
    def release(self):
        """
        Releases the mutex. Must be called by a thread that acquired the mutex
        """
        self.__jmutex.release()
        
    def __enter__(self):
        self.acquire()
        return self
    
    def __exit__(self, t, v):
        self.release()