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
package me.enerccio.sp.types.types;

import me.enerccio.sp.compiler.Bytecode;
import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.compiler.PythonBytecode.*;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

/**
 * bytecode()
 * @author Enerccio
 *
 */
public class BytecodeTypeObject extends TypeObject {
	private static final long serialVersionUID = 1434651099156262641L;
	public static final String BYTECODE_CALL = "bytecode";

	@Override
	public String getTypeIdentificator() {
		return "bytecode";
	}

	@SuppressWarnings("unused")
	@Override
	public PythonObject call(TupleObject args) {
		if (args.len() == 0)
			throw Utils.throwException("TypeError", "bytecode(): incorrect number of parameters, must be >0");
		
		try {
			IntObject byteNum = (IntObject) args.getObjects()[0];
			
			Bytecode b = Bytecode.fromNumber((int) byteNum.intValue());
			if (b == null)
				throw Utils.throwException("TypeError", "unknown bytecode number");
			PythonBytecode bytecode = null;
			
			switch (b) {
			case CALL:
				bytecode = new Call();
				bytecode.newObject();
				bytecode.value = args.getObjects()[1];
				break;
			case DUP:
				bytecode = new Dup();
				bytecode.newObject();
				break;
			case GOTO:
				bytecode = new Goto();
				bytecode.newObject();
				bytecode.intValue = (int) ((IntObject) args.getObjects()[1]).intValue();
				break;
			case JUMPIFFALSE:
				bytecode = new JumpIfFalse();
				bytecode.newObject();
				bytecode.intValue = (int) ((IntObject) args.getObjects()[1]).intValue();
				break;
			case JUMPIFTRUE:
				bytecode = new JumpIfTrue();
				bytecode.newObject();
				bytecode.intValue = (int) ((IntObject) args.getObjects()[1]).intValue();
				break;
			case LOAD:
				bytecode = new Load();
				bytecode.newObject();
				bytecode.stringValue = ((StringObject) args.getObjects()[1]).value;
				break;
			case LOADGLOBAL:
				bytecode = new LoadGlobal();
				bytecode.newObject();
				bytecode.stringValue = ((StringObject) args.getObjects()[1]).value;
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
				bytecode.value = args.getObjects()[1];
				break;
			case PUSH_DICT:
				bytecode = new PushDict();
				bytecode.newObject();
				bytecode.mapValue = (MapObject) args.getObjects()[1];
				break;
			case PUSH_ENVIRONMENT:
				bytecode = new PushEnvironment();
				bytecode.newObject();
				break;
//				bytecode.ast = new ArrayList<PythonBytecode>(Utils.asList(args.getObjects()[1]));
//				for (PythonBytecode pb : bytecode.ast)
//					; // just check that it is all inded python bytecode not some other crap data
//				break;
			case RETURN:
				bytecode = new Return();
				bytecode.newObject();
				bytecode.value = args.getObjects()[1];
				break;
			case SAVE:
				bytecode = new Save();
				bytecode.newObject();
				bytecode.stringValue = ((StringObject) args.getObjects()[1]).value;
				break;
			case SAVEGLOBAL:
				bytecode = new SaveGlobal();
				bytecode.newObject();
				bytecode.stringValue = ((StringObject) args.getObjects()[1]).value;
				break;
			case IMPORT:
				bytecode = new Import();
				bytecode.stringValue2 = ((StringObject) args.getObjects()[1]).value;
				bytecode.stringValue = ((StringObject) args.getObjects()[2]).value;
				bytecode.newObject();
				break;
			case SWAP_STACK:
				bytecode = new SwapStack();
				bytecode.newObject();
				break;
			case UNPACK_SEQUENCE:
				bytecode = new UnpackSequence();
				bytecode.newObject();
				bytecode.intValue = (int) ((IntObject) args.getObjects()[1]).intValue();
				break;
			case PUSH_LOCAL_CONTEXT:
				bytecode = new PushLocalContext();
				bytecode.newObject();
				bytecode.value = args.getObjects()[1];
				break;
			case RESOLVE_ARGS:
				bytecode = new ResolveArgs();
				bytecode.newObject();
				break;
			}
			
			return bytecode;
		} catch (ClassCastException e){
			throw Utils.throwException("TypeError", "bytecode(): incorrect type of arguments");
		} catch (ArrayIndexOutOfBoundsException e){
			throw Utils.throwException("TypeError", "bytecode(): incorrect number of arguments");
		}
		
	}
}
