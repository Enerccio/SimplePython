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

_threadinfo = javainstance("__threadinfo__")

class Thread(object):
    """
    Thread object. Can be started with start method.
    Executes execute method in a new thread
    """
    def __init__(self, name=None, thread=None, daemon=False):
        if thread is None:
            self.__jthread = javainstance("__jthread__", self, name)
        else:
            self.__jthread = thread
        self.__jthread.setDaemon(daemon)
        self.executed = getattr(self.__jthread, "executed")
        
    def set_name(self, name):
        """
        Sets the name of the thread to name
        """
        self.__jthread.setThreadName(name)
        
    def get_name(self):
        """
        Returns name of the thread
        """
        return self.__jthread.getThreadName()
        
    def start(self):
        """
        Starts the thread. Can only be called once
        """
        self.__jthread.threadStart()
        
    def execute(self):
        """
        This method is executed in a new thread. Throws NotImplementedError by default,
        so it should be overwritten by classes extending Thread
        """
        raise NotImplementedError("thread.execute")
    
    def join(self):
        """
        Waits until this thread finishes
        """
        self.__jthread.waitJoin()
        
    def interrupt(self):
        """
        Interrupts this thread
        """
        self.__jthread.interruptThread()
        
    def running(self):
        """
        Returns True if this thread is running
        """
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
        @Staticmethod
        Waits ms milliseconds
        """
        _threadinfo.wait_time(ms, 0)
        
    @staticmethod
    def wait_nanotime(ms, ns):
        """
        @Staticmethod
        Waits time milliseconds and ns nanoseconds
        """
        _threadinfo.wait_time(ms, ns)
