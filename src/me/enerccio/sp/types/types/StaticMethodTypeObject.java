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
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.Tags;
import me.enerccio.sp.types.callables.UserFunctionObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.types.system.StaticMethodObject;
import me.enerccio.sp.utils.Utils;

/**
 * bool()
 * 
 * @author Enerccio
 *
 */
public class StaticMethodTypeObject extends TypeObject {
	private static final long serialVersionUID = 6840091655610067377L;
	public static final String STATIC_METHOD_CALL = "staticmethod";

	@Override
	public String getTypeIdentificator() {
		return "staticmethod";
	}

	@Override
	public PythonObject call(TupleObject args, KwArgs kwargs) {
		if (kwargs != null)
			kwargs.notExpectingKWArgs(); // Throws exception if there is kwarg
											// defined
		if (args.len() != 1)
			throw new TypeError("staticmethod(): requires 1 argument");
		PythonObject o = args.get(0);
		if (!(o instanceof UserFunctionObject)) {
			throw new TypeError(
					"staticmethod(): argument must be a python function");
		}
		StaticMethodObject smo = new StaticMethodObject();
		Utils.putPublic(smo, StaticMethodObject.__FUNC__, o);
		return smo;
	}

	@Override
	public byte getTag() {
		return Tags.SMETH_TYPE;
	}
}
