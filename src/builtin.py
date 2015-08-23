"""
SimplePython - embeddable python interpret in java
Copyright (c) Peter Vanusanik, All rights reserved.
 
Builtin module
 
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

# all python doable builtin functions are here

from exceptions import *

import os
import sys
import math

def abs(x):
    """
    Returns absolute value of x
    Relies on __gt__ and __sub__ for object x
    """
    if x > 0:
        return x
    return -x

def all(iterable):
    """
    Returns True if every value in iterable is true
    """
    for i in iterable:
        if not i: return False
    return True

def any(iterable):
    """
    Returns True if at least one value is true
    """ 
    for i in iterable:
        if i: return True 
    return False

def callable(object):
    """
    Returns True if object is callable
    """
    if object is None: return False
    t = type(object)
    if t == function: return True
    if t == method: return True
    if t == type: return True
    if t == javamethod: return True
    return hasattr(object, "__call__")

def iterable(object):
    """
    Returns true if object is iterable or can be made into iterable (has __iter__ or __getitem__)
    """
    if hasattr(object, "__iter__"):
        return True
    if hasattr(object, "__getitem__"):
        return True
    return False

def cmp(x, y):
    """
    Compares x and y, returns -1 if x < y, 0 if x == y and 1 if x > y
    """
    if x < y:
        return -1
    elif x == y:
        return 0
    else:
        return 1
    
def divmod(a, b):
    """
    Returns pair containing a/b and a%b
    """
    return int(math.floor(a/b)), int(a%b)

class iter(object):
    """
    Transforms object into iterator, if possible. If itervalue is callable, callable_iter is created
    """
    class callable_iterator(object):
        """
        callable_iterator represents iterator made of callable and sentinel value
        """
        def __init__(self, fnc, __sentinel):
            self.__sentinel = __sentinel
            self.__fnc = fnc
        def __iter__(self):
            return self
        def next(self):
            value = self.__fnc()
            if (value == self.__sentinel):
                raise StopIteration()
            return value
    
    def __init__(self, itervalue, sentinel=None):
        if hasattr(itervalue, "__iter__"):
            return itervalue.__iter__()
        elif callable(itervalue):
            return sentinel_iter(itervalue, sentinel)
        else:
            self.__data_source = itervalue
            self.__index = 0
            
    def __iter__(self):
        return self
        
    def next(self):
        try:
            i = self.__index
            self.__index += 1
            return self.__data_source[i]
        except IndexError:
            raise StopIteration()

class enumerate(object):
    """
    Creates new enumerate class that returns values of provided sequence in pairs
    (i, seq[i])
    """
    def __init__(self, seq, start=0):
        self.__it = iter(seq)
        self.__start = 0
        
    def __iter__(self):
        return self
    
    def next(self):
        i = self.__start
        self.__start += 1
        return i, self.__it.next()
    
class file(object):
    """
    opens new file based on name - path, and mode
    """
    def __init__(self, name, mode="r"):
        self.name = name
        self.mode = mode
        self.closed = False
        
        self.__data_stream = javainstance("__filestream__", self.name, self.mode)
        self.encoding = self.__data_stream.encoding()
    
    def __enter__(self):
        return self
    
    def __exit__(self, type, value):
        self.close()
        
    def check_closed(self):
        """
        Raises exception if file is closed.
        Called internally to check if file was closed or not
        """
        if self.closed:
            raise ValueError("file already closed")
        
    def close(self):
        """
        Closes the file object. Raises exception if already closed
        """
        self.check_closed()
        self.__data_stream.close()
        self.closed = True
        
    def flush(self):
        """
        Flushes values into the underline object.
        Since java RandomAccess file does not support flushing, this is a no op, only
        checking if file has been closed or not
        """
        self.check_closed()
        
    def fileno(self):
        """
        Returns underline fileno object, as provided from java. Does not return integer as normal python would
        """
        self.check_closed()
        return self.__data_stream.fileno();
    
    def isatty(self):
        """
        Returns whether underline file is atty
        """
        self.check_closed()
        return self.__data_stream.isatty();
    
    def __iter__(self):
        return self
    
    def next(self):
        """
        Reads next line from file
        """
        self.check_closed()
        data = self.readline()
        if data == "":
            raise StopIteration()
        return data
        
    def read(self, size=1):
        """
        Reads data from file, the amount provided by size
        """
        self.check_closed()
        return self.__data_stream.read(size)
    
    def realine(self, size=0):
        """
        Reads line from file and returns it or reads at most size amount
        """
        buffer = ""
        while True:
            c = self.read()
            if c == "":
                return buffer
            if c == "\n":
                return buffer + "\n"
            buffer += c
            if (size > 0) and len(buffer) == size:
                return buffer
            
    def seek(self, offset, whence=os.SEEK_SET):
        """
        Sets the file position to offset based on whence type
        """
        self.check_closed()
        self.__data_stream.seek(offset, whence);
        
    def tell(self):
        """
        Returns actual position in the file
        """
        self.check_closed()
        return self.__data_stream.tell()
    
    def truncate(self, size=0):
        """
        Truncates the file to the size. However, java RandomAccessFile does not support it,
        so it is a no op and only checks whether file has been closed or not
        """
        self.check_closed() # not available
    
    def write(self, str):
        """
        Writes string str into the stream at current position
        """
        self.check_closed()
        self.__data_stream.write(str)
        
    def writeline(self, seq):
        """
        Writes elements in seq into a stream
        """
        for x in iter(seq):
            self.write(x)
            
    def read_all(self):
        """
        Reads whole file into a string
        """
        value = ""
        while True:
            n = self.read(256)
            if n == "":
                return value
            value += n
        
def filter(f, it):
    """
    Filters values into a list based on the callable provided. This callable must take 1 argument and based on
    truth value of the result of the call, object will be appended into a list or not
    """
    l = []
    for i in it:
        if f(i):
            l.append(i)
    return l

def format(value, format_spec=""):
    """
    Formats value according to format_spec
    """
    return value.__format__(format_spec)

def issubclass(cls, classinfo):
    if type(classinfo) == tuple:
        for c in classinfo:
            if issubclass(cls, c):
                return True
        return False
    return classinfo in mro(cls)

def len(s):
    if hasattr(s, "__len__"):
        return s.__len__()
    raise TypeError("len(): argument does not support len protocol")

def identity(*args):
    """
    Idenitity function, returns it's arguments back
    """
    if len(args) == 0:
        return args[0]
    return args

def map(function, *iterables):
    """
    Maps the iterables via function into a new list
    """
    if function is None:
        function = identity
    data = [iter(x) for x in iterables]
    result = []
    while True:
        d = []
        r = []
        for iterable in data:
            try:
                d.append(iterable.next())
                r.append(True)
            except StopIteration:
                d.append(None)
                r.append(False)
        if not any(r):
            return result
        result.append(apply(function, d))
        
def open(name, mode="r"):
    """
    Opens new file with name and mode. Name is path
    """
    return file(name, mode)

def pow(x, y, z=1):
    """
    Returns (x**y) % z
    """
    return (x**y) % z;

class set(object):
    """
    Creates new set containing provided iterable
    """
    def __init__(self, iterable=None):
        if iterable is None:
        	self.__inner_map = dict()
        else:
        	self.__inner_map = { o : o for o in iterable }
    
    def __str__(self):
        return "set(" + str(self.__inner_map.keys()) + ")"
    
    def add(self, o):
        """
        Adds element to the set
        """
        self.__inner_map[o] = o;
        
    def __contains__(self, key):
        return key in self.__inner_map
    
    def __delkey__(self, key):
        del self.__inner_map[key]
    
def print_function(objects, sep=' ', end='\n', file=sys.stdout):
    """
    Representing normal print statement. Prints objects with sep inserted inbetween and at the end prints end. 
    All printing is done to the provided file which must support write method
    """
    if file is None:
         file = sys.stdout
    first = True
    for o in objects:
        if not first:
            file.write(sep)
        else:
            first = False
        file.write(str(o))
    file.write(end)
    
def print_function_fnc(*objects, **kwargs):
    """
    Replacement for print_function if print_function is imported, then it calls this, which in 
    effect calls print_function
    """
    print_function(objects, **kwargs)
    
   
def range(arg1, arg2=None, arg3=1):
    """
    Creates list of ranges based on the arguments
    """
    if arg2 is None:
        return list(xrange(arg1))
    return list(xrange(arg1, arg2, arg3))

class super(object):
    """
    Creates a super object that allows access to correct method resolution order
    """
    def __init__(self, cls, inst=None):
        self.__cls = cls
        self.__inst = inst
        self.__mro = mro(cls)[1:]
        
    def __getattribute__(self, key):
        cls, arg = object.__getattribute__(self, "__find_applicable")(key);
        if type(arg) == boundfunction:
            if object.__getattribute__(self, "__inst") is not None:
                return method(arg.__func__, object.__getattribute__(self, "__inst"), cls)
            else:
                return arg
        else:
            return arg
        
    def __find_applicable(self, key):
        for cls in object.__getattribute__(self, "__mro"):
            if (hasattr(cls, key)):
                return cls, getattr(cls, key)
        raise AttributeError("unknown attribute " + key)
    
def close_generator(generator):
    """
    Correctly closes the generator, raises RuntimeError if generator does not close correctly
    """
    try:
        generator.throw(GeneratorExit, None)
    except (GeneratorExit, StopIteration):
        pass
    else:
        raise RuntimeError("generator ignored GeneratorExit")
    
def typename(object):
    """
    Returns typename of an object, shortcut to object.__type__.__name__
    """
    return type(object).__name__

def zip(*iterables):
    """
    Creates zip view of the iterables, ie pairs of every value of iterable
    """
    data = [iter(x) for x in iterables]
    result = []
    while True:
        d = []
        for iterable in data:
            try:
                d.append(iterable.next())
            except StopIteration:
                return result
        result.append(tuple(d))
        
def sum(iterable):
    """
    Returns sum of all objects in iterable
    """
    it = 0
    for x in iterable:
        it += x
    return it
