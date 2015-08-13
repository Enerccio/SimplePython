#!/bin/python2

"""
SimplePython - embeddable python interpret in java
Copyright (c) Peter Vanusanik, All rights reserved.
 
 setup.py for initializing java generated classes
 
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

def math_interpreter_template():
    template_path = "src/me/enerccio/sp/interpret/InterpreterMathExecutorHelper.java.template"
    output_path = "gen/me/enerccio/sp/interpret/InterpreterMathExecutorHelper.java"
    
    operations = ["ADD", "SUB", "MUL", "DIV", "MOD", "AND", "OR", "XOR", "POW", "RSHIFT", "LSHIFT",
                  "LT", "LE", "GE", "GT", "EQ", "NE"]
    op_lower = [s.lower() for s in operations]
    op_camel = [s[0] + s[1:].lower() for s in operations]
    
    template_interface = """
    public interface Has%sMethod {
        public PythonObject %s(PythonObject o);
    }"""
    
    template_switch = """
        case %s: {
                PythonObject b = stack.pop();
                PythonObject a = stack.pop();
                if (!(a instanceof ClassInstanceObject) && (a instanceof Has%sMethod))
                    stack.push(((Has%sMethod)a).%s(b));
                else
                    standardCall(interpreter, o, "__%s__", a, b);
            } break;"""
    
    template = open(template_path, "r").read()
    
    tc = len(operations)
    interfaces = ""
    switches = ""
    
    for i in xrange(tc):
        interface = template_interface % (op_camel[i], op_lower[i])
        interfaces += interface + "\n"
        switch = template_switch % (operations[i], op_camel[i], op_camel[i], op_lower[i], op_lower[i])
        switches += switch
        
    template = template.replace("$$interfaces$$", interfaces)
    template = template.replace("$$switches$$", switches)
    
    output = open(output_path, "w").write(template)

if __name__ == "__main__":
    math_interpreter_template()