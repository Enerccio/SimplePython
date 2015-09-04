/*
 * SimplePython - embeddable python interpret in java
 * Copyright (c) Peter Vanusanik, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package me.enerccio.sp.interpreter.debug;

import me.enerccio.sp.compiler.Bytecode;
import me.enerccio.sp.interpret.FrameObject;
import me.enerccio.sp.interpret.PythonInterpreter;

public interface Debugger {
	
	public void bind(PythonInterpreter pythonInterpreter);
	
	public void debugNextOperation(PythonInterpreter i, Bytecode b, FrameObject f, int cpc);
	
	public void unbind(PythonInterpreter pythonInterpreter); // called after the debugger has been unbound in interpreter

}
