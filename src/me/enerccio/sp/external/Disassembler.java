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
package me.enerccio.sp.external;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.TreeMap;

import me.enerccio.sp.compiler.Bytecode;
import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.errors.StopIteration;
import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.interpret.CompiledBlockObject.DebugInformation;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.sandbox.PythonSecurityManager.SecureAction;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.NumberObject;
import me.enerccio.sp.types.mappings.DictObject;
import me.enerccio.sp.types.pointer.WrapAnnotationFactory.WrapField;
import me.enerccio.sp.types.pointer.WrapAnnotationFactory.WrapMethod;
import me.enerccio.sp.utils.CastFailedException;
import me.enerccio.sp.utils.Coerce;

public class Disassembler {

	private byte[] data;
	private DictObject mappings;
	private TreeMap<Integer, DebugInformation> debug;
	private ByteBuffer readBuff;
	@WrapField(readOnly = true)
	public int last_bytecode_pos;
	
	public Disassembler(String code, DictObject mappings, TreeMap<Integer, DebugInformation> dmap){
		PythonRuntime.runtime.checkSandboxAction("disassembler", SecureAction.DISASSEMBLY, code, mappings, dmap);
		this.data = getCode(code);
		this.mappings = mappings;
		this.debug = dmap;
		this.readBuff = ByteBuffer.wrap(data);
	}
	
	private byte[] getCode(String code) {
		if (code.startsWith("\\x"))
			code = code.substring(2);
		String[] spl = code.split("\\\\x");
		byte[] arr = new byte[spl.length];
		for (int i=0; i<spl.length; i++)
			arr[i] = (byte) (Short.parseShort(spl[i], 16) & 0xFF);
		return arr;
	}

	@WrapMethod
	public Disassembler __iter__(){
		return this;
	}
	
	@WrapMethod
	public synchronized PythonObject next(){
		try {
			return doGetNext();
		} catch (BufferUnderflowException e){
			throw new StopIteration();
		}
	}
	
	private PythonObject doGetNext(){
		if (readBuff.remaining() == 0)
			throw new StopIteration();
		
		last_bytecode_pos = readBuff.position();
		DebugInformation d = debug.get(debug.floorKey(last_bytecode_pos));
		
		Bytecode b = Bytecode.fromNumber(((short) (readBuff.get() & 0xff)));
		PythonBytecode bytecode = Bytecode.makeBytecode(b, null, d.function, d.module);
		bytecode.debugCharacter = d.charno;
		bytecode.debugLine = d.lineno;
		bytecode.newObject();
		
		try {
			switch (b) {
			case ACCEPT_ITER:
			case CALL:
			case DUP:
			case ECALL:
			case RCALL:
			case RETURN:
			case SETUP_LOOP:
			case TRUTH_VALUE:
			case UNPACK_SEQUENCE:
			case KCALL:
			case GET_ITER:
			case GOTO:
			case JUMPIFFALSE:
			case JUMPIFNONE:
			case JUMPIFNORETURN:
			case JUMPIFTRUE:
			case SWAP_STACK:
			case MAKE_FIRST:
				bytecode.intValue = readBuff.getInt();
				break;
			case PUSH_FRAME:
				bytecode.intValue = readBuff.getInt();
				bytecode.object = readBuff.getInt();
				break;
			case DELATTR:
			case LOAD:
			case LOADBUILTIN:
			case LOADDYNAMIC:
			case LOADGLOBAL:
			case SAVE:
			case SAVEDYNAMIC:
			case SAVEGLOBAL:
			case SAVE_LOCAL:
			case TEST_FUTURE:
			case SETATTR:
				bytecode.stringValue = Coerce.toJava(mappings.doGet(NumberObject.valueOf(readBuff.getInt())), String.class);
				break;
			case MAKE_FUTURE:
			case KWARG:
				bytecode.object = Coerce.toJava(mappings.doGet(NumberObject.valueOf(readBuff.getInt())), String[].class);
				break;
			case YIELD:
				bytecode.stringValue = Coerce.toJava(mappings.doGet(NumberObject.valueOf(readBuff.getInt())), String.class);
				bytecode.intValue = Coerce.toJava(mappings.doGet(NumberObject.valueOf(readBuff.getInt())), int.class);
				break;
			case IMPORT:
				bytecode.stringValue = Coerce.toJava(mappings.doGet(NumberObject.valueOf(readBuff.getInt())), String.class);
				bytecode.object = Coerce.toJava(mappings.doGet(NumberObject.valueOf(readBuff.getInt())), String.class);
				break;
			case PUSH:
				bytecode.value = mappings.doGet(NumberObject.valueOf(readBuff.getInt()));
				break;
			case DEL:
				bytecode.stringValue = Coerce.toJava(mappings.doGet(NumberObject.valueOf(readBuff.getInt())), String.class);
				bytecode.booleanValue = readBuff.getInt() == 1;
				break;
			case GETATTR:
			case ISINSTANCE:
			case NOP:
			case OPEN_LOCALS:
			case POP:
			case PUSH_ENVIRONMENT:
			case PUSH_EXCEPTION:
			case PUSH_LOCALS:
			case PUSH_LOCAL_CONTEXT:
			case RAISE:
			case RERAISE:
			case RESOLVE_ARGS:
			case RESOLVE_CLOSURE:
			case UNPACK_KWARG:
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
				break;
			}
		} catch (CastFailedException e){
			throw new TypeError("bytecode internal failure", e);
		}
		
		return bytecode;
	}
}
