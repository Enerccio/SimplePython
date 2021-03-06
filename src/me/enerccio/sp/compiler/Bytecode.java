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

import me.enerccio.sp.compiler.PythonBytecode.AcceptIter;
import me.enerccio.sp.compiler.PythonBytecode.BinaryOperator;
import me.enerccio.sp.compiler.PythonBytecode.Call;
import me.enerccio.sp.compiler.PythonBytecode.Debug;
import me.enerccio.sp.compiler.PythonBytecode.Del;
import me.enerccio.sp.compiler.PythonBytecode.DelAttr;
import me.enerccio.sp.compiler.PythonBytecode.Dup;
import me.enerccio.sp.compiler.PythonBytecode.ECall;
import me.enerccio.sp.compiler.PythonBytecode.GetAttr;
import me.enerccio.sp.compiler.PythonBytecode.GetIter;
import me.enerccio.sp.compiler.PythonBytecode.Goto;
import me.enerccio.sp.compiler.PythonBytecode.Import;
import me.enerccio.sp.compiler.PythonBytecode.IsInstance;
import me.enerccio.sp.compiler.PythonBytecode.JumpIfFalse;
import me.enerccio.sp.compiler.PythonBytecode.JumpIfNoReturn;
import me.enerccio.sp.compiler.PythonBytecode.JumpIfNone;
import me.enerccio.sp.compiler.PythonBytecode.JumpIfTrue;
import me.enerccio.sp.compiler.PythonBytecode.KCall;
import me.enerccio.sp.compiler.PythonBytecode.KwArg;
import me.enerccio.sp.compiler.PythonBytecode.Load;
import me.enerccio.sp.compiler.PythonBytecode.LoadBuiltin;
import me.enerccio.sp.compiler.PythonBytecode.LoadDynamic;
import me.enerccio.sp.compiler.PythonBytecode.LoadFuture;
import me.enerccio.sp.compiler.PythonBytecode.LoadGlobal;
import me.enerccio.sp.compiler.PythonBytecode.MakeFirst;
import me.enerccio.sp.compiler.PythonBytecode.MakeFuture;
import me.enerccio.sp.compiler.PythonBytecode.Nop;
import me.enerccio.sp.compiler.PythonBytecode.OpenLocals;
import me.enerccio.sp.compiler.PythonBytecode.Pop;
import me.enerccio.sp.compiler.PythonBytecode.Push;
import me.enerccio.sp.compiler.PythonBytecode.PushEnvironment;
import me.enerccio.sp.compiler.PythonBytecode.PushException;
import me.enerccio.sp.compiler.PythonBytecode.PushFrame;
import me.enerccio.sp.compiler.PythonBytecode.PushLocalContext;
import me.enerccio.sp.compiler.PythonBytecode.PushLocals;
import me.enerccio.sp.compiler.PythonBytecode.RCall;
import me.enerccio.sp.compiler.PythonBytecode.Raise;
import me.enerccio.sp.compiler.PythonBytecode.Reraise;
import me.enerccio.sp.compiler.PythonBytecode.ResolveArgs;
import me.enerccio.sp.compiler.PythonBytecode.ResolveClosure;
import me.enerccio.sp.compiler.PythonBytecode.Return;
import me.enerccio.sp.compiler.PythonBytecode.Save;
import me.enerccio.sp.compiler.PythonBytecode.SaveDynamic;
import me.enerccio.sp.compiler.PythonBytecode.SaveGlobal;
import me.enerccio.sp.compiler.PythonBytecode.SaveLocal;
import me.enerccio.sp.compiler.PythonBytecode.SetAttr;
import me.enerccio.sp.compiler.PythonBytecode.SetupLoop;
import me.enerccio.sp.compiler.PythonBytecode.SwapStack;
import me.enerccio.sp.compiler.PythonBytecode.TestFuture;
import me.enerccio.sp.compiler.PythonBytecode.TruthValue;
import me.enerccio.sp.compiler.PythonBytecode.UnpackKwArg;
import me.enerccio.sp.compiler.PythonBytecode.UnpackSequence;
import me.enerccio.sp.compiler.PythonBytecode.Yield;
import me.enerccio.sp.types.ModuleObject.ModuleData;

import org.antlr.v4.runtime.Token;

/**
 * Bytecode enum, containing types of bytecodes and their numerical value.
 * 
 * @author Enerccio
 *
 */
public enum Bytecode {
	// System
	NOP(0), PUSH_ENVIRONMENT(8), RESOLVE_CLOSURE(10), PUSH_LOCAL_CONTEXT(11), IMPORT(
			12), RESOLVE_ARGS(13), PUSH_FRAME(15), PUSH_EXCEPTION(16), OPEN_LOCALS(
			17), PUSH_LOCALS(18),

	// control
	POP(31), PUSH(32), CALL(33), KCALL(34), RCALL(35), ECALL(36), DUP(37), SWAP_STACK(
			38), JUMPIFTRUE(39), JUMPIFFALSE(40), JUMPIFNONE(41), JUMPIFNORETURN(
			42), GOTO(43), RETURN(44), SAVE_LOCAL(45), TRUTH_VALUE(46), MAKE_FIRST(
			47),
	// variables
	LOAD(64), LOADGLOBAL(65), SAVE(66), SAVEGLOBAL(67), UNPACK_SEQUENCE(68), LOADDYNAMIC(
			69), SAVEDYNAMIC(70), LOADBUILTIN(71),
	// special call-related
	KWARG(80), UNPACK_KWARG(81), MAKE_FUTURE(82), TEST_FUTURE(83), LOAD_FUTURE(
			84),
	// exceptions
	RAISE(85), RERAISE(86),
	// macros
	GETATTR(89), SETATTR(90), ISINSTANCE(91),
	// frames
	YIELD(96),
	// delete
	DEL(104), DELATTR(105),

	// loops, iterators, boolean stuff
	SETUP_LOOP(128), GET_ITER(129), ACCEPT_ITER(130),

	// math macros
	ADD(164), SUB(165), MUL(166), DIV(167), MOD(168), AND(169), OR(170), XOR(
			171), POW(172), RSHIFT(173), LSHIFT(174), LT(175), LE(176), GE(177), GT(
			178), EQ(179), NE(180), DCOLON(181), QM(182), RARROW(183), AT(184),

	// debugger only bytecodes
	D_STARTFUNC(250), D_RETURN(251), ;

	Bytecode(int id) {
		this.id = id;
	}

	public static final String NO_FUNCTION = "<module>";

	public final int id;

	private static final Bytecode[] bytecodes;

	static {
		bytecodes = new Bytecode[256];
		for (Bytecode b : values())
			bytecodes[b.id] = b;
	}

	/**
	 * Transforms int into bytecode or null if no such bytecode exists
	 * 
	 * @param intValue
	 * @return
	 */
	public static Bytecode fromNumber(int intValue) {
		Bytecode b = bytecodes[intValue];
		if (b == null)
			throw new NullPointerException("bytecode not found: " + intValue);
		return b;
	}

	/**
	 * Creates new PythonBytecode object based on the bytecode, sets the source
	 * information based on the token.
	 * 
	 * @param b
	 * @param t
	 *            may be null
	 * @return
	 */
	public static PythonBytecode makeBytecode(Bytecode b, Token t,
			String functionName, ModuleData module) {
		PythonBytecode bytecode = null;

		switch (b) {
		case CALL:
			bytecode = new Call();

			break;
		case DEL:
			bytecode = new Del();

			break;
		case TEST_FUTURE:
			bytecode = new TestFuture();

			break;
		case DELATTR:
			bytecode = new DelAttr();

			break;
		case RESOLVE_CLOSURE:
			bytecode = new ResolveClosure();

			break;
		case LOADBUILTIN:
			bytecode = new LoadBuiltin();

			break;
		case PUSH_LOCALS:
			bytecode = new PushLocals();

			break;
		case RCALL:
			bytecode = new RCall();

			break;
		case KCALL:
			bytecode = new KCall();

			break;
		case ECALL:
			bytecode = new ECall();

			break;
		case ACCEPT_ITER:
			bytecode = new AcceptIter();

			break;
		case TRUTH_VALUE:
			bytecode = new TruthValue();

			break;
		case DUP:
			bytecode = new Dup();

			break;
		case GOTO:
			bytecode = new Goto();

			break;
		case JUMPIFFALSE:
			bytecode = new JumpIfFalse();

			break;
		case JUMPIFTRUE:
			bytecode = new JumpIfTrue();

			break;
		case JUMPIFNONE:
			bytecode = new JumpIfNone();

			break;
		case JUMPIFNORETURN:
			bytecode = new JumpIfNoReturn();

			break;
		case LOAD:
			bytecode = new Load();

			break;
		case LOADGLOBAL:
			bytecode = new LoadGlobal();

			break;
		case LOAD_FUTURE:
			bytecode = new LoadFuture();

			break;
		case MAKE_FUTURE:
			bytecode = new MakeFuture();

			break;
		case NOP:
			bytecode = new Nop();

			break;
		case POP:
			bytecode = new Pop();

			break;
		case MAKE_FIRST:
			bytecode = new MakeFirst();

			break;
		case PUSH:
			bytecode = new Push();

			break;
		case PUSH_ENVIRONMENT:
			bytecode = new PushEnvironment();

			break;
		case RETURN:
			bytecode = new Return();

			break;
		case SAVE:
			bytecode = new Save();

			break;
		case SAVEGLOBAL:
			bytecode = new SaveGlobal();

			break;
		case SAVEDYNAMIC:
			bytecode = new SaveDynamic();

			break;
		case LOADDYNAMIC:
			bytecode = new LoadDynamic();

			break;
		case IMPORT:
			bytecode = new Import();

			break;
		case SWAP_STACK:
			bytecode = new SwapStack();

			break;
		case KWARG:
			bytecode = new KwArg();

			break;
		case UNPACK_KWARG:
			bytecode = new UnpackKwArg();

			break;
		case UNPACK_SEQUENCE:
			bytecode = new UnpackSequence();

			break;
		case PUSH_LOCAL_CONTEXT:
			bytecode = new PushLocalContext();

			break;
		case RESOLVE_ARGS:
			bytecode = new ResolveArgs();

			break;
		case GETATTR:
			bytecode = new GetAttr();

			break;
		case SETATTR:
			bytecode = new SetAttr();

			break;
		case ISINSTANCE:
			bytecode = new IsInstance();

			break;
		case RAISE:
			bytecode = new Raise();

			break;
		case RERAISE:
			bytecode = new Reraise();

			break;
		case PUSH_FRAME:
			bytecode = new PushFrame();

			break;
		case PUSH_EXCEPTION:
			bytecode = new PushException();

			break;
		case SAVE_LOCAL:
			bytecode = new SaveLocal();

			break;
		case SETUP_LOOP:
			bytecode = new SetupLoop();

			break;
		case GET_ITER:
			bytecode = new GetIter();

			break;
		case YIELD:
			bytecode = new Yield();

			break;
		case OPEN_LOCALS:
			bytecode = new OpenLocals();

			break;
		case ADD:
		case AND:
		case DIV:
		case EQ:
		case GE:
		case GT:
		case LE:
		case LSHIFT:
		case LT:
		case MOD:
		case MUL:
		case NE:
		case OR:
		case POW:
		case RSHIFT:
		case SUB:
		case XOR:
		case DCOLON:
		case QM:
		case RARROW:
		case AT:
			bytecode = new BinaryOperator(b);

			break;
		case D_STARTFUNC:
		case D_RETURN:
			bytecode = new Debug(b);

			break;
		}

		if (module == null)
			throw new RuntimeException(
					"Module cannot be null. Create static ModuleProvider if necessary.");
		bytecode.debugModule = module;
		bytecode.debugFunction = functionName == null ? NO_FUNCTION
				: functionName;
		if (t != null) {
			bytecode.debugLine = t.getLine();
			bytecode.debugCharacter = t.getCharPositionInLine();
			bytecode.debugFunction = functionName;
		}

		return bytecode;
	}

	/**
	 * Disassembles the list of bytecodes
	 * 
	 * @param bcl
	 * @return
	 */
	public static String dis(List<PythonBytecode> bcl) {
		return dis(bcl, 0);
	}

	/**
	 * Disassembles single bytecode
	 * 
	 * @param i
	 * @param bc
	 * @return
	 */
	public static String dis(int i, PythonBytecode bc) {
		String s = bc.toString();
		int cut = s.length();
		if (cut > 80)
			cut = 80;
		return String.format("%05d \t%s", i, s.substring(0, cut));
	}

	/**
	 * Disassembles the list of bytecodes from offset bytecode
	 * 
	 * @param i
	 * @param bc
	 * @return
	 */
	public static String dis(List<PythonBytecode> bcl, int offset) {
		StringBuilder b = new StringBuilder();
		for (int i = offset; i < bcl.size(); i++) {
			PythonBytecode bc = bcl.get(i);
			b.append(dis(i, bc));
			b.append("\n");
		}
		return b.toString();
	}
}
