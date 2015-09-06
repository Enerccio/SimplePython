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

import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.interpret.AbstractPythonInterpreter;
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.Tags;
import me.enerccio.sp.types.base.NumberObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;

/**
 * int()
 * 
 * @author Enerccio
 *
 */
public class LongTypeObject extends TypeObject {
	private static final long serialVersionUID = -4178003762513900453L;
	public static final String LONG_CALL = "long";

	public LongTypeObject() {

	}

	@Override
	public String getTypeIdentificator() {
		return "long";
	}

	@Override
	public PythonObject call(TupleObject args, KwArgs kwargs) {
		PythonObject base = kwargs.consume("base");
		kwargs.checkEmpty("long");
		if (args.len() < 1)
			return NumberObject.valueOf(0l);
		if (args.len() > 2)
			throw new TypeError("long() takes at most 2 arguments");
		PythonObject obj = args.get(0);
		if (args.len() == 2) {
			if (base != null)
				throw new TypeError(
						"long() got duplicate value for argument 'base'");
			base = args.get(1);
		}
		if (base != null) {
			if (!NumberObject.isInteger(base))
				throw new TypeError("long() base argument must be integer");
			if (!(obj instanceof StringObject))
				throw new TypeError(
						"long() can't convert non-string with explicit base");
			return NumberObject.valueOf(Long.valueOf(obj.toString(),
					((NumberObject) base).intValue()));
		}

		if (obj instanceof StringObject)
			return NumberObject.valueOf(Long.valueOf(obj.toString()));
		if (obj instanceof NumberObject)
			return NumberObject.valueOf(((NumberObject) obj).longValue());
		PythonObject __int__ = obj.get(NumberObject.__INT__, null);
		if (__int__ != null) {
			int cfc = AbstractPythonInterpreter.interpreter.get().currentFrame
					.size();
			AbstractPythonInterpreter.interpreter.get().execute(false, __int__,
					null);
			return AbstractPythonInterpreter.interpreter.get().executeAll(cfc);
		}

		throw new TypeError("long() can't convert " + obj.toString()
				+ " to long");
	}

	@Override
	public byte getTag() {
		return Tags.LONG_TYPE;
	}
}
