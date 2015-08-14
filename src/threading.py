"""
SimplePython - embeddable python interpret in java
Copyright (c) Peter Vanusanik, All rights reserved.
 
threading module
 
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

__all__ = ["Thread"]

_threadinfo = javainstance("threadinfo")

class Thread(object):
    def __init__(self, name=None, thread=None, daemon=False):
        if thread is None:
            self.__jthread = javainstance("jthread", self, name)
        else:
            self.__jthread = thread
        self.__jthread.setDaemon(daemon)
        self.executed = getattr(self.__jthread, "executed")
        
    def set_name(self, name):
        self.__jthread.setThreadName(name)
        
    def get_name(self):
        return self.__jthread.getThreadName()
        
    def start(self):
        self.__jthread.threadStart()
        
    def execute(self):
        raise NotImplementedError("thread.execute")
    
    def join(self):
        self.__jthread.waitJoin()
        
    def interrupt(self):
        self.__jthread.interruptThread()
        
    def running(self):
        return self.__jthread.threadRunning()
    
    def __eq__(self, other):
        if hasattr(other, "__jthread"):
            return self.__jthread == other.__jthread
        return False
    
    def __str__(self):
        return "<Thread '" + self.get_name() + ", jthread='" + str(self.__jthread) + "'>"
    
    @staticmethod
    def current_thread():
        """
        Returns current thread as new instance of Thread object
        """
        return Thread(None, _threadinfo.current_thread())
    
    @staticmethod
    def wait(ms):
        """
        Waits ms milliseconds
        """
        _threadinfo.wait_time(ms, 0)
        
    @staticmethod
    def wait_nanotime(ms, ns):
        """
        Waits time milliseconds and ns nanoseconds
        """
        _threadinfo.wait_time(ms, ns)
