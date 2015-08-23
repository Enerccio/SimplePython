"""
SimplePython - embeddable python interpret in java
Copyright (c) Peter Vanusanik, All rights reserved.
 
future module
 
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

__all__ = ["print_function"]

_PRINT_FUNCTION = 0x1

class __FutureObject(object):
    """
    Represents Future implementation.
    Does nothing, really, only prints some nice information.
    Real future is modyfing the compiler
    """
    def __init__(self, future_id, version_id, version_impl):
        self.future_id = future_id
        self.version_id = version_id
        self.version_impl = version_impl
    
    def __str__(self):
        return "Future[id=" + self.future_id + ", v=" + self.version_id + ", impl=" + self.version_impl + "]"
    
print_function = __FutureObject(_PRINT_FUNCTION, "2.7", "3.0")