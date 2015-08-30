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
import me.enerccio.sp.types.base.ClassInstanceObject;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.callables.UserFunctionObject;
import me.enerccio.sp.types.callables.UserMethodObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

/**
 * method()
 * 
 * @author Enerccio
 *
 */
public class MethodTypeObject extends TypeObject {
	private static final long serialVersionUID = 6537509851545433991L;
	public static final String METHOD_CALL = "method";

	@Override
	public String getTypeIdentificator() {
		return "method";
	}

	@Override
	public PythonObject call(TupleObject args, KwArgs kwargs) {
		if (kwargs != null)
			kwargs.notExpectingKWArgs(); // Throws exception if there is kwarg
											// defined
		if (args.len() != 3)
			throw new TypeError(
					"method(): wrong mumber of parameters, requires 3, got "
							+ args.len());

		UserFunctionObject fnc;
		ClassInstanceObject inst;
		ClassObject accessor;
		try {
			fnc = (UserFunctionObject) args.valueAt(0);
			inst = (ClassInstanceObject) args.valueAt(1);
			accessor = (ClassObject) args.valueAt(2);
		} catch (ClassCastException e) {
			throw new TypeError(
					"method(): wrong types of parameters. Parameter 1 must be function and parameter 2 must be instance");
		}

		UserMethodObject mo = new UserMethodObject();
		Utils.putPublic(mo, UserMethodObject.FUNC, fnc);
		Utils.putPublic(mo, UserMethodObject.SELF, inst);
		Utils.putPublic(mo, UserMethodObject.ACCESSOR, accessor);
		return mo;
	}
	
	@Override
	public byte getTag() {
		return Tags.METH_TYPE;
	}
}
