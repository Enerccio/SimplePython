"""
SimplePython - embeddable python interpret in java
Copyright (c) Peter Vanusanik, All rights reserved.
 
 Exceptions module 
 
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

class Error(object):
    def __init__(self, message=None):
        self.__message__ = message
        self.stack = []
        
    def __str__(self):
        return str(self.__class__) + ": " + str(self.__message__) 
    
class SandboxViolationError(Error):
    pass
        
class BaseException(Error):
    pass

class Exception(BaseException):
    pass

class LoopBreak(Exception):
    pass

class LoopContinue(Exception):
    pass

class StandardError(Exception):
    pass

class ArithmeticError(StandardError):
    pass

class BufferError(StandardError):
    pass

class LookupError(StandardError):
    pass

class EnvironmentError(StandardError):
    pass

class AttributeError(StandardError):
    pass

class EOFError(StandardError):
    pass

class IOError(StandardError):
    pass

class ImportError(StandardError):
    pass

class IndexError(LookupError):
    pass

class KeyError(LookupError):
    pass

class KeyboardInterrupt(BaseException):
    pass

class MemoryError(StandardError):
    pass

class NameError(StandardError):
    pass

class RuntimeError(StandardError):
    pass

class NotImplementedError(RuntimeError):
    pass

class OSError(StandardError):
    pass

class StopIteration(Exception):
    pass

class SyntaxError(StandardError):
    pass

class SystemError(Exception):
    pass

class SystemExit(BaseException):
    def __init__(self, status):
        super(BaseException, self).__init__(str(status))
        self.exit = status

class TypeError(StandardError):
    pass

class ValueError(StandardError):
    pass

class GeneratorExit(Exception):
    pass

class NativeError(Exception):
    pass

class InterpreterError(Exception):
    pass
