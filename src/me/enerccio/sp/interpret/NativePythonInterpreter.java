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

import java.util.Stack;

import me.enerccio.sp.compiler.Bytecode;
import me.enerccio.sp.types.PythonObject;

public class NativePythonInterpreter extends AbstractPythonInterpreter {
	
	NativePythonInterpreter() {
		super();
	}

	@Override
	protected native ExecutionResult doExecuteSingleInstruction(FrameObject o,
			Stack<PythonObject> stack, Bytecode opcode);

	@Override
	protected native void resolveClosure(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void openLocals(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void pushLocals(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void pushEnvironment(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void call(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void setupLoop(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void acceptIter(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native boolean getIter(FrameObject o, Stack<PythonObject> stack) ;

	@Override
	protected native void ecall(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void krcall(Bytecode opcode, FrameObject o,
			Stack<PythonObject> stack);

	@Override
	protected native void gotoOperation(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void jumpIfFalse(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void jumpIfTrue(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void jumpIfNone(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void jumpIfNoReturn(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void makeFuture(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void load(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void loadFuture(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void testFuture(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void loadGlobal(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void pop(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void truthValue(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void push(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void returnOperation(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void save(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void kwarg(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void saveLocal(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void saveGlobal(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void dup(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void importOperation(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void swapStack(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void makeFirst(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void unpackSequence(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void unpackKwargs(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void pushLocalContext(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void resolveArgs(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void getAttr(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void delAttr(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void setAttr(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void isinstance(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void raise(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void reraise(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void pushFrame(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void pushException(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native ExecutionResult yield(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void loadDynamic(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void saveDynamic(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void loadBuiltin(FrameObject o, Stack<PythonObject> stack);

	@Override
	protected native void del(FrameObject o, Stack<PythonObject> stack);

}
