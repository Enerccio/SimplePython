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

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.BoundHandleObject;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.callables.UserFunctionObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

/**
 * boundfunction()
 * @author Enerccio
 *
 */
public class BoundFunctionTypeObject extends TypeObject {
	private static final long serialVersionUID = 1755292062829138174L;
	public static final String BOUND_FUNCTION_CALL = "boundfunction";

	@Override
	public String getTypeIdentificator() {
		return "bound-function";
	}

	@Override
	public PythonObject call(TupleObject args) {
		if (args.len() != 3)
			throw Utils.throwException("TypeError", "boundfunction(): wrong mumber of parameters, requires 2, got " + args.len());
		
		UserFunctionObject fnc;
		ClassObject accessor;
		try {
			fnc = (UserFunctionObject) args.valueAt(0);
			accessor = (ClassObject) args.valueAt(1);
		} catch (ClassCastException e){
			throw Utils.throwException("TypeError", "boundfunction(): wrong types of parameters. Parameter 1 must be function and parameter 2 must be a type");
		}
		
		BoundHandleObject bh = new BoundHandleObject();
		bh.newObject();
		Utils.putPublic(bh, BoundHandleObject.FUNC, fnc);
		Utils.putPublic(bh, BoundHandleObject.ACCESSOR, accessor);
		return bh;
	}
}
