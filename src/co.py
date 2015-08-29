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

def __generator_wrapper(arg):
    generator = arg()
    generator.next()
    return generator

class coroutine(object):
    """
    Creates new coroutine. Callable must be a callable that requires zero arguments
    """
    def __init__(self, callable):
        self.__callable = callable
        self.__state = SUSPENDED
        self.__future = None
        self.__generator = None
        self.__argmap = {}
        self.__itc = 0
        self.__terminated = False
        
    def __check_terminate(self):
        if self.__terminated:
            self.__state = DEAD
            raise RuntimeError("terminated")
        
    def terminate(self):
        """
        Forcefully terminates the coroutine. All threads waiting on resume will get
        RuntimeError, inner function of coroutine will get RuntimeError as well
        """
        self.__terminated = True
        
    def resume(self, arg=__Noarg()):
        """
        Resumes the coroutine. First call must have zero arguments, 
        next calls can pass value. Will block and wait for coroutine's next yield.
        """
        if self.__state == DEAD:
            raise TypeError("resume(): coroutine is already dead")
        
        first = False
        if self.__generator is None:
            if not isinstance(arg, __Noarg):
                raise TypeError("resume(): first call to resume must not have an argument")
            
            self.__next_future(0, True)
            first = True
        
        if not first:
            if isinstance(arg, __Noarg):
                arg = None
            self.__argmap[self.__itc] = arg
            self.__itc += 1
            if self.__itc > 1:
                self.__itc = 0
        
        while (self.__state == RUNNING): 
            self.__check_terminate()
        
        try:
            value = self.__future
        except Error, e:
            self.__state = DEAD
            return None
        
        self.__next_future(self.__itc, False)
        return value    
    
    def status(self):
        """
        Returns status of the coroutine
        """
        return self.__state
        
    def __next_future(self, itc, first):
        self.__state = RUNNING
        
        f = None
        if first:
            f = future: self._generate_generator()
        else:
            f = future: self._next_generator_value(itc)
        
        self.__future = future f
        
            
    def _generate_generator(self):
        self.__generator = __generator_wrapper(self.__callable)
        self.__state = SUSPENDED
        return None
        
    def _next_generator_value(self, itc):
        arg = future: self._get_arg(itc)
        value = self.__generator.send(future arg)
        self.__state = SUSPENDED
        return value
    
    def _get_arg(self, argid):
        while argid not in self.__argmap:
            self.__check_terminate()
        arg = self.__argmap[argid]
        del self.__argmap[argid]
        return arg