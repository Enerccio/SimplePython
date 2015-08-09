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

class AST(object):
    def __init__(self):
        super(AST, self).__init__()
        
        self.__ast = []
        
    def add_bytecode(self, bytecode):
        if not type(bytecode) == bytecode:
            raise TypeError, "bytecode must be type 'bytecode'"
        self.__ast.append(bytecode)
        
    def get_bytecode(self):
        return list(self.__ast)
