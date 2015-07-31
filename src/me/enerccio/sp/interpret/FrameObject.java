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

import java.nio.ByteBuffer;
import java.util.Stack;

import me.enerccio.sp.compiler.Bytecode;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.iterators.GeneratorObject;

/**
 * FrameObject represents execution frame. Holds flags related to the execution and also pc + bytecode
 * @author Enerccio
 *
 */
public class FrameObject extends PythonObject {
	private static final long serialVersionUID = 3202634156179178037L;
	
	/**
	 * Parent frame is null if this is normal frame, or reference to parent frame if this is a subframe
	 */
	public FrameObject parentFrame;
	/** whether previous frame ended with return */
	public boolean returnHappened;
	/** whether this frame pushed local context or not*/

	/** whether this frame wants to accept some value as return */
	public boolean accepts_return;
	
	/** If python exception has happened, it will be stored here */
	public PythonObject exception;
	/** Bytecode of this frame */
	public CompiledBlockObject compiled;
	/** Bytebuffer of the data */
	public ByteBuffer dataStream;
	/** program counter */
	public int pc;
	/** python stack of this frame */
	public Stack<PythonObject> stack = new Stack<PythonObject>();
	public GeneratorObject ownedGenerator;
	public PythonObject localContext;
	public EnvironmentObject environment;

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

	public KwArgs.HashMapKWArgs kwargs = null;

	public Bytecode nextOpcode() {
		++pc;
		return Bytecode.fromNumber(dataStream.get());
	}
	
	public int nextInt(){
		pc += 4;
		return dataStream.getInt();
	}
}
