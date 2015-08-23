"""
SimplePython - embeddable python interpret in java
Copyright (c) Peter Vanusanik, All rights reserved.
 
 sys module
 
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

__all__ = ["stdout", "stderr", "exit", "current_time"]

stdout = javainstance("__sysoutstream__", False)
stderr = javainstance("__sysoutstream__", True)

__system = javainstance("__system__")

def exit(status=0):
    """
    Raises SystemExit with status provided
    """
    raise SystemExit(status)

def current_time():
    """
    Returns current time in ms
    """
    return __system.current_time()