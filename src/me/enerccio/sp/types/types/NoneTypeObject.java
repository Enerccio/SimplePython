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
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.sequences.TupleObject;

public class NoneTypeObject extends TypeObject {
	private static final long serialVersionUID = 3817336724216604451L;
	public static final String NONE_TYPE_CALL = "none";

	@Override
	public String getTypeIdentificator() {
		return "NoneType";
	}

	@Override
	public PythonObject call(TupleObject args, KwArgs kwargs){
		if (kwargs != null)
			kwargs.notExpectingKWArgs();
		if (args.len() != 0)
			throw new TypeError("none(): requires zero arguments");
		return NoneObject.NONE;
	}
}
