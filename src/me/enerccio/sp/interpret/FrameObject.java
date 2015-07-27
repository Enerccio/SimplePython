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
package me.enerccio.sp.interpret;

import java.util.List;
import java.util.Stack;

import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.types.PythonObject;

public class FrameObject extends PythonObject {
	private static final long serialVersionUID = 3202634156179178037L;
	
	public FrameObject parentFrame;
	public boolean returnHappened;
	public boolean pushed_context;
	public boolean pushed_environ;
	
	public PythonObject exception;
	public List<PythonBytecode> bytecode;
	public int pc;
	public Stack<PythonObject> stack = new Stack<PythonObject>();

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	protected String doToString() {
		return "<frame object 0x" + Integer.toHexString(hashCode()) + ">";
	}
	
	public String debugModule;
	public int debugLine;
	public int debugInLine;
}
