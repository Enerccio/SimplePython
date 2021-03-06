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
import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.Tags;
import me.enerccio.sp.types.base.ClassInstanceObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

/**
 * str()
 * 
 * @author Enerccio
 *
 */
public class StringTypeObject extends TypeObject {
	private static final long serialVersionUID = 189813854164565772L;
	public static final String STRING_CALL = "str";

	@Override
	public String getTypeIdentificator() {
		return STRING_CALL;
	}

	@Override
	public PythonObject call(TupleObject args, KwArgs kwargs) {
		if (kwargs != null)
			kwargs.notExpectingKWArgs(); // Throws exception if there is kwarg
											// defined
		if (args.len() != 1)
			throw new TypeError("str(): incorrect number of parameters");

		PythonObject o = args.getObjects()[0];
		if (o instanceof ClassInstanceObject) {
			int cfc = PythonInterpreter.interpreter.get().currentFrame
					.size();
			Utils.run("getattr", args.getObjects()[0], new StringObject(
					"__str__"));
			PythonObject ret = PythonInterpreter.interpreter.get()
					.executeAll(cfc);
			return PythonInterpreter.interpreter.get().execute(false,
					ret, null);
		} else
			return new StringObject(o.toString());
	}

	@Override
	public void newObject() {
		super.newObject();
	}

	@Override
	public byte getTag() {
		return Tags.STRING_TYPE;
	}
}
