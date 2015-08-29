"""
SimplePython - embeddable python interpret in java
Copyright (c) Peter Vanusanik, All rights reserved.
 
coroutine module
 
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

__all__ = ["SUSPENDED", "DEAD", "RUNNING", "coroutine"]

class __Noarg(object):
    pass

SUSPENDED = "suspended"
DEAD = "dead"
RUNNING = "running"

class coroutine(object):
    def __init__(self, callable):
        self.__callable = callable
        self.__state = SUSPENDED
        self.__future = None
        self.__future_query  = None
        self.__generator = None
        
    def resume(self, arg=__Noarg()):
        if self.__state == DEAD:
            raise TypeError("resume(): coroutine is already dead")
        
        if self.__future_query is None:
            if not isinstance(arg, __Noarg):
                raise TypeError("resume(): first call to resume must not have an argument")
            
            self.__next_future(arg)
            return
        
        fq = self.__future_query
        while (not ready fq):
            pass
        
        try:
            value = self.__future
        except:
            self.__state = DEAD
            return None
        
        __next_future(arg)
        return value
    
    def status(self):
        return self.__state
        
    def __next_future(self, arg):
        self.__state = RUNNING
        
        f = None
        if isinstance(arg, __Noarg):
            # first run
            f = future: self.__generate_generator()
        else:
            f = future: self.__next_generator_value(arg)
        
        self.__future = future f
        self.__future_query = future_object(future f)
        
            
    def __generate_generator(self):
        self.__generator = self.__callable()
        
    def __next_generator_value(self, arg):
        value = self.__generator.send(arg)
        self.__state = SUSPENDED
        return value