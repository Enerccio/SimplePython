"""
SimplePython - embeddable python interpret in java
Copyright (c) Peter Vanusanik, All rights reserved.
 
ast module
 
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

__all__ = ["dis"]

def dis(object):
    """
    Disassembles the object, if is a object that can be disassembled into standard stream
    """
    
    if type(object) == compiled_block:
        dis_compiled_block(object)
        

def dis_compiled_block(cb):
    dass = javainstance("__disassembler__", cb.co_code, cb.co_consts, cb.co_debug)
    last_lineno = -1
    for bytecode in dass:
        s = ""
        if last_lineno == -1:
           print "function " + bytecode._function + ": "
        if bytecode._lineno > last_lineno:
            s += str(bytecode._lineno) + "\t"
            last_lineno = bytecode._lineno 
        else:
            s += "\t"
        
        s += str(dass.last_bytecode_pos) + "\t"
        s += str(bytecode)
        print s