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
import me.enerccio.sp.types.sequences.TupleObject;

public class EnvironmentTypeObject extends TypeObject {
	private static final long serialVersionUID = -5475655736158235162L;
	public static final String ENVIRONMENT_CALL = "environment";

	@Override
	public String getTypeIdentificator() {
		return "environment";
	}

	@Override
	public PythonObject call(TupleObject args, KwArgs kwargs) {
		if (args.len() > 0)
			throw new TypeError("environment(): requiring 0 arguments, got "
					+ args.len());
		if (kwargs != null)
			kwargs.checkEmpty("environment");
		return PythonInterpreter.interpreter.get().environment();
	}

	@Override
	public byte getTag() {
		return Tags.ENV_TYPE;
	}
}
