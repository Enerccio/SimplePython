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

NOP=0
PUSH_ENVIRONMENT=8
RESOLVE_CLOSURE=10
PUSH_LOCAL_CONTEXT=11 
IMPORT=12 
RESOLVE_ARGS=13 
PUSH_FRAME=15 
PUSH_EXCEPTION=16
OPEN_LOCALS=17 
PUSH_LOCALS=18
POP=32 
PUSH=33 
CALL=34 
RCALL=35 
ECALL=36 
DUP=37
SWAP_STACK=38 
JUMPIFTRUE=39 
JUMPIFFALSE=40 
JUMPIFNONE=41
JUMPIFNORETURN=42 
GOTO=43 
RETURN=44 
SAVE_LOCAL=45
TRUTH_VALUE=46
LOAD=64 
LOADGLOBAL=65 
SAVE=66 
SAVEGLOBAL=67 
UNPACK_SEQUENCE=68 
LOADDYNAMIC=69 
SAVEDYNAMIC=70 
LOADBUILTIN=71
KWARG=80
RAISE=82 
RERAISE=83
GETATTR=89 
SETATTR=90 
ISINSTANCE=91 
YIELD=96
DEL=104 
DELATTR=105
SETUP_LOOP=128 
GET_ITER=129 
ACCEPT_ITER=130

def get_bytecode_types():
    bytecode_names = {}
    bytecode_numbers = {}
    g = globals()
    for name in g.keys():
        value = g[name]
        if type(value) == int:
            bytecode_names[value] = name
            bytecode_numbers[name] = value
    return bytecode_names, bytecode_numbers

BYTECODE_NAMES, BYTECODE_NUMBERS = get_bytecode_types()

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
