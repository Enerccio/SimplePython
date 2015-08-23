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

import java.util.Map;
import java.util.TreeMap;

import me.enerccio.sp.compiler.Bytecode;
import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.interpret.ModuleResolver;
import me.enerccio.sp.types.ModuleObject.ModuleData;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.NumberObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.properties.MethodPropertyObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.CastFailedException;
import me.enerccio.sp.utils.Coerce;
import me.enerccio.sp.utils.Utils;

/**
 * bytecode()
 * 
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

	@Override
	public void newObject() {
		super.newObject();

		try {
			Utils.putPublic(this, "names", new MethodPropertyObject("names",
					JavaMethodObject.noArgMethod(this, "names")));
			Utils.putPublic(this, "numbers", new MethodPropertyObject("names",
					JavaMethodObject.noArgMethod(this, "numbers")));
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("kurva", e);
		}
	}

	public Map<String, Integer> names() {
		Map<String, Integer> tMap = new TreeMap<String, Integer>();
		for (Bytecode b : Bytecode.values())
			tMap.put(b.name(), b.id);
		return tMap;
	}

	public Map<Integer, String> numbers() {
		Map<Integer, String> tMap = new TreeMap<Integer, String>();
		for (Bytecode b : Bytecode.values())
			tMap.put(b.id, b.name());
		return tMap;
	}

	private static ModuleData mook = new ModuleData() {
		@Override
		public String getName() {
			return "<generated-module>";
		}

		@Override
		public String getFileName() {
			return "<generated-module>";
		}

		@Override
		public ModuleResolver getResolver() {
			return null;
		}

		@Override
		public String getPackageResolve() {
			return "";
		}

		@Override
		public boolean isPackage() {
			return false;
		}
	};

	@Override
	public PythonObject call(TupleObject args, KwArgs kwargs) {
		if (args.len() == 0)
			throw new TypeError(
					"bytecode(): incorrect number of parameters, must be >0");

		try {
			NumberObject byteNum = (NumberObject) args.getObjects()[0];

			Bytecode b = Bytecode.fromNumber(byteNum.intValue());
			if (b == null)
				throw new TypeError("bytecode(): unknown bytecode number "
						+ byteNum);
			PythonBytecode bytecode = Bytecode
					.makeBytecode(b, null, null, mook);
			bytecode.newObject();

			switch (b) {
			case ACCEPT_ITER:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case MAKE_FIRST:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case CALL:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case DEL:
				bytecode.stringValue = Coerce.toJava(args.get(1), String.class);
				bytecode.booleanValue = Coerce.toJava(args.get(2),
						boolean.class);
				break;
			case DELATTR:
				bytecode.stringValue = Coerce.toJava(args.get(1), String.class);
				break;
			case DUP:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case ECALL:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case GETATTR:
				break;
			case GET_ITER:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case GOTO:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case IMPORT:
				bytecode.stringValue = Coerce.toJava(args.get(1), String.class);
				bytecode.object = Coerce.toJava(args.get(2), String.class);
				break;
			case ISINSTANCE:
				break;
			case JUMPIFFALSE:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case JUMPIFNONE:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case JUMPIFNORETURN:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case JUMPIFTRUE:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case KWARG:
				bytecode.object = Coerce.toJava(args.get(1), String[].class);
				break;
			case LOAD:
				bytecode.stringValue = Coerce.toJava(args.get(1), String.class);
				break;
			case LOADBUILTIN:
				bytecode.stringValue = Coerce.toJava(args.get(1), String.class);
				break;
			case LOADDYNAMIC:
				bytecode.stringValue = Coerce.toJava(args.get(1), String.class);
				break;
			case LOADGLOBAL:
				bytecode.stringValue = Coerce.toJava(args.get(1), String.class);
				break;
			case TEST_FUTURE:
				bytecode.stringValue = Coerce.toJava(args.get(1), String.class);
				break;
			case MAKE_FUTURE:
				bytecode.object = Coerce.toJava(args.get(1), String[].class);
				break;
			case NOP:
				break;
			case OPEN_LOCALS:
				break;
			case POP:
				break;
			case PUSH:
				bytecode.value = args.get(1);
				break;
			case PUSH_ENVIRONMENT:
				break;
			case PUSH_EXCEPTION:
				break;
			case PUSH_FRAME:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				bytecode.object = Coerce.toJava(args.get(2), int.class);
				break;
			case PUSH_LOCALS:
				break;
			case PUSH_LOCAL_CONTEXT:
				break;
			case RAISE:
				break;
			case RCALL:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case RERAISE:
				break;
			case RESOLVE_ARGS:
				break;
			case RESOLVE_CLOSURE:
				break;
			case RETURN:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case SAVE:
				bytecode.stringValue = Coerce.toJava(args.get(1), String.class);
				break;
			case SAVEDYNAMIC:
				bytecode.stringValue = Coerce.toJava(args.get(1), String.class);
				break;
			case SAVEGLOBAL:
				bytecode.stringValue = Coerce.toJava(args.get(1), String.class);
				break;
			case SAVE_LOCAL:
				bytecode.stringValue = Coerce.toJava(args.get(1), String.class);
				break;
			case SETATTR:
				bytecode.stringValue = Coerce.toJava(args.get(1), String.class);
				break;
			case SETUP_LOOP:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case SWAP_STACK:
				break;
			case TRUTH_VALUE:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case UNPACK_KWARG:
				break;
			case UNPACK_SEQUENCE:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case YIELD:
				bytecode.stringValue = Coerce.toJava(args.get(1), String.class);
				bytecode.intValue = Coerce.toJava(args.get(2), int.class);
				break;
			case KCALL:
				bytecode.intValue = Coerce.toJava(args.get(1), int.class);
				break;
			case ADD:
				break;
			case AND:
				break;
			case DIV:
				break;
			case EQ:
				break;
			case GE:
				break;
			case GT:
				break;
			case LE:
				break;
			case LSHIFT:
				break;
			case LT:
				break;
			case MOD:
				break;
			case MUL:
				break;
			case NE:
				break;
			case OR:
				break;
			case POW:
				break;
			case RSHIFT:
				break;
			case SUB:
				break;
			case XOR:
				break;
			case DCOLON:
				break;
			case QM:
				break;
			case RARROW:
				break;
			case LOAD_FUTURE:
				bytecode.stringValue = Coerce.toJava(args.get(1), String.class);
				break;
			default:
				break;
			}

			bytecode.newObject();
			return bytecode;
		} catch (CastFailedException e) {
			throw new TypeError("bytecode(): incorrect type of arguments");
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new TypeError("bytecode(): incorrect number of arguments");
		}

	}
}
