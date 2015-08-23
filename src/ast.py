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

def get_bytecode_types():
    """
    Returns names of bytecodes and their numbers
    """
    bytecode_names = bytecode.names
    bytecode_numbers = bytecode.numbers
    g = globals()
    for name in bytecode_names.keys():
        g[name] = bytecode_names[name]
    return bytecode_names, bytecode_numbers

BYTECODE_NAMES, BYTECODE_NUMBERS = get_bytecode_types()

class AST(object):
    """
    AST object represents the list of bytecodes.
    This class is necessary so it can be guaranteed that ast provided
    is only containing bytecode objects
    """
    def __init__(self):
        super(AST, self).__init__()
        
        self.__ast = []
        
    def add_bytecode(self, bytecode):
        """
        Adds the bytecode to this ast.
        Throws TypeError if object is not a bytecode
        """
        if not type(bytecode) == bytecode:
            raise TypeError, "bytecode must be type 'bytecode'"
        self.__ast.append(bytecode)
        
    def get_bytecode(self):
        """
        Returns new copy of bytecode inside this AST
        """
        return list(self.__ast)

__all__ = ["AST", "BYTECODE_NAMES", "BYTECODE_TYPES"] + BYTECODE_NAMES.keys()