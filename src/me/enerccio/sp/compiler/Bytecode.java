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
package me.enerccio.sp.compiler;

import java.util.List;

import org.antlr.v4.runtime.Token;

import me.enerccio.sp.compiler.PythonBytecode.*;

/**
 * Bytecode enum, containing types of bytecodes and their numerical value. 
 * @author Enerccio
 *
 */
public enum Bytecode {
	// System
	NOP(0), 
	PUSH_ENVIRONMENT(8), PUSH_DICT(10), PUSH_LOCAL_CONTEXT(11), 
	IMPORT(12), RESOLVE_ARGS(13), PUSH_FRAME(15), PUSH_EXCEPTION(16),
	
	// control
	POP(17), PUSH(18), CALL(19), RCALL(20), ECALL(21), DUP(22),
	SWAP_STACK(23), JUMPIFTRUE(24), JUMPIFFALSE(25), JUMPIFNONE(26),
	JUMPIFNORETURN(27), GOTO(28), RETURN(29), SAVE_LOCAL(30),
	TRUTH_VALUE(31),
	// variables
	LOAD(32), LOADGLOBAL(33), SAVE(35), SAVEGLOBAL(36), UNPACK_SEQUENCE(37),
	// special call-related
	KCALL(40), KWARG(41),
	// exceptions
	RAISE(69), RERAISE(70),
	// macros
	GETATTR(90), SETATTR(91), ISINSTANCE(92), 
	// frames 
	
	// loops, iterators, boolean stuff
	SETUP_LOOP(100), GET_ITER(101), ACCEPT_ITER(102),
	;
	
	Bytecode(int id){
		this.id = id;
	}
	
	public final int id;

	/**
	 * Transforms int into bytecode or null if no such bytecode exists
	 * @param intValue
	 * @return
	 */
	public static Bytecode fromNumber(int intValue) {
		for (Bytecode b : values())
			if (b.id == intValue)
				return b;
		return null;
	}
	
	/**
	 * Creates new PythonBytecode object based on the bytecode
	 * @param b
	 * @return
	 */
	public static PythonBytecode makeBytecode(Bytecode b) {
		return makeBytecode(b, null);
	}

	/**
	 * Creates new PythonBytecode object based on the bytecode, sets the source information based on the token.
	 * @param b
	 * @param t may be null
	 * @return
	 */
	public static PythonBytecode makeBytecode(Bytecode b, Token t) {
		PythonBytecode bytecode = null;
		
		switch (b) {
		case CALL:
			bytecode = new Call();
			bytecode.newObject();
			break;
		case RCALL:
			bytecode = new RCall();
			bytecode.newObject();
			break;
		case ECALL:
			bytecode = new ECall();
			bytecode.newObject();
			break;
		case ACCEPT_ITER:
			bytecode = new AcceptIter();
			bytecode.newObject();
			break;
		case TRUTH_VALUE:
			bytecode = new TruthValue();
			bytecode.newObject();
			break;
		case DUP:
			bytecode = new Dup();
			bytecode.newObject();
			break;
		case GOTO:
			bytecode = new Goto();
			bytecode.newObject();
			break;
		case JUMPIFFALSE:
			bytecode = new JumpIfFalse();
			bytecode.newObject();
			break;
		case JUMPIFTRUE:
			bytecode = new JumpIfTrue();
			bytecode.newObject();
			break;
		case JUMPIFNONE:
			bytecode = new JumpIfNone();
			bytecode.newObject();
			break;
		case JUMPIFNORETURN:
			bytecode = new JumpIfNoReturn();
			bytecode.newObject();
			break;
		case LOAD:
			bytecode = new Load();
			bytecode.newObject();
			break;
		case LOADGLOBAL:
			bytecode = new LoadGlobal();
			bytecode.newObject();
			break;
		case NOP:
			bytecode = new Nop();
			bytecode.newObject();
			break;
		case POP:
			bytecode = new Pop();
			bytecode.newObject();
			break;
		case PUSH:
			bytecode = new Push();
			bytecode.newObject();
			break;
		case PUSH_DICT:
			bytecode = new PushDict();
			bytecode.newObject();
			break;
		case PUSH_ENVIRONMENT:
			bytecode = new PushEnvironment();
			bytecode.newObject();
			break;
		case RETURN:
			bytecode = new Return();
			bytecode.newObject();
			break;
		case SAVE:
			bytecode = new Save();
			bytecode.newObject();
			break;
		case SAVEGLOBAL:
			bytecode = new SaveGlobal();
			bytecode.newObject();
			break;
		case IMPORT:
			bytecode = new Import();
			bytecode.newObject();
			break;
		case SWAP_STACK:
			bytecode = new SwapStack();
			bytecode.newObject();
			break;
		case KCALL:
			bytecode = new KCall();
			bytecode.newObject();
			break;
		case KWARG:
			bytecode = new KwArg();
			bytecode.newObject();
			break;
		case UNPACK_SEQUENCE:
			bytecode = new UnpackSequence();
			bytecode.newObject();
			break;
		case PUSH_LOCAL_CONTEXT:
			bytecode = new PushLocalContext();
			bytecode.newObject();
			break;
		case RESOLVE_ARGS:
			bytecode = new ResolveArgs();
			bytecode.newObject();
			break;
		case GETATTR:
			bytecode = new GetAttr();
			bytecode.newObject();
			break;
		case SETATTR:
			bytecode = new SetAttr();
			bytecode.newObject();
			break;
		case ISINSTANCE:
			bytecode = new IsInstance();
			bytecode.newObject();
			break;
		case RAISE:
			bytecode = new Raise();
			bytecode.newObject();
			break;
		case RERAISE:
			bytecode = new Reraise();
			bytecode.newObject();
			break;
		case PUSH_FRAME:
			bytecode = new PushFrame();
			bytecode.newObject();
			break;
		case PUSH_EXCEPTION:
			bytecode = new PushException();
			bytecode.newObject();
			break;
		case SAVE_LOCAL:
			bytecode = new SaveLocal();
			bytecode.newObject();
			break;
		case SETUP_LOOP:
			bytecode = new SetupLoop();
			bytecode.newObject();
			break;
		case GET_ITER:
			bytecode = new GetIter();
			bytecode.newObject();
			break;
		}
		
		bytecode.debugModule = PythonCompiler.moduleName.get();
		if (t != null){
			bytecode.debugLine = t.getLine();
			bytecode.debugInLine = t.getCharPositionInLine();
		}
		
		return bytecode;
	}
	
	/**
	 * Disassembles the list of bytecodes
	 * @param bcl
	 * @return
	 */
	public static String dis(List<PythonBytecode> bcl) {
		return dis(bcl, 0);
	}
	
	/**
	 * Disassembles single bytecode
	 * @param i
	 * @param bc
	 * @return
	 */
	public static String dis(int i, PythonBytecode bc) {
		String s = bc.toString();
		int cut = s.length();
		if (cut > 80) cut  = 80;
		return String.format("%05d \t%s" , i, s.substring(0, cut));
	}

	
	/**
	 * Disassembles the list of bytecodes from offset bytecode 
	 * @param i
	 * @param bc
	 * @return
	 */
	public static String dis(List<PythonBytecode> bcl, int offset) {
		StringBuilder b = new StringBuilder();
		for (int i=offset; i<bcl.size(); i++) {
			PythonBytecode bc = bcl.get(i);
			b.append(dis(i, bc));
			b.append("\n");
		}
		return b.toString();
	}
}
