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
import me.enerccio.sp.interpret.CompiledBlockObject.DebugInformation;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.sandbox.PythonSecurityManager.SecureAction;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.mappings.DictObject;
import me.enerccio.sp.types.pointer.WrapAnnotationFactory.WrapField;
import me.enerccio.sp.types.pointer.WrapAnnotationFactory.WrapMethod;
import me.enerccio.sp.utils.CastFailedException;
import me.enerccio.sp.utils.Coerce;
import me.enerccio.sp.utils.Utils;

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
			throw Utils.throwException("StopIteration");
		}
	}
	
	private PythonObject doGetNext(){
		if (readBuff.remaining() == 0)
			Utils.throwException("StopIteration");
		
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
			case PUSH_FRAME:
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
				bytecode.intValue = readBuff.getInt();
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
			case SETATTR:
				bytecode.stringValue = Coerce.toJava(mappings.doGet(IntObject.valueOf(readBuff.getInt())), String.class);
				break;
			case KWARG:
				bytecode.object = Coerce.toJava(mappings.doGet(IntObject.valueOf(readBuff.getInt())), String[].class);
				break;
			case YIELD:
				bytecode.stringValue = Coerce.toJava(mappings.doGet(IntObject.valueOf(readBuff.getInt())), String.class);
				bytecode.intValue = Coerce.toJava(mappings.doGet(IntObject.valueOf(readBuff.getInt())), int.class);
				break;
			case IMPORT:
				bytecode.stringValue = Coerce.toJava(mappings.doGet(IntObject.valueOf(readBuff.getInt())), String.class);
				bytecode.object = Coerce.toJava(mappings.doGet(IntObject.valueOf(readBuff.getInt())), String.class);
				break;
			case PUSH:
				bytecode.value = mappings.doGet(IntObject.valueOf(readBuff.getInt()));
				break;
			case DEL:
				bytecode.stringValue = Coerce.toJava(mappings.doGet(IntObject.valueOf(readBuff.getInt())), String.class);
				bytecode.booleanValue = readBuff.getInt() == 1;
				break;
			case GETATTR:
				break;
			case ISINSTANCE:
				break;
			case NOP:
				break;
			case OPEN_LOCALS:
				break;
			case POP:
				break;
			case PUSH_ENVIRONMENT:
				break;
			case PUSH_EXCEPTION:
				break;
			case PUSH_LOCALS:
				break;
			case PUSH_LOCAL_CONTEXT:
				break;
			case RAISE:
				break;
			case RERAISE:
				break;
			case RESOLVE_ARGS:
				break;
			case RESOLVE_CLOSURE:
				break;
			case SWAP_STACK:
				break;
			case UNPACK_KWARG:
				break;
			}
		} catch (CastFailedException e){
			throw Utils.throwException("TypeError", "bytecode internal failure", e);
		}
		
		return bytecode;
	}
}
