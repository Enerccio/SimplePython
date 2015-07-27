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
import me.enerccio.sp.types.base.SliceObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class SliceTypeObject extends TypeObject {
	private static final long serialVersionUID = 1174044496063617044L;
	public static final String SLICE_CALL = "slice";

	@Override
	public String getTypeIdentificator() {
		return "splice";
	}

	@Override
	public PythonObject call(TupleObject args) {
		if (args.len() != 3)
			throw Utils.throwException("TypeError", "slice(): incorrect number of parameters, must be 3");
		
		return new SliceObject(args.valueAt(0), args.valueAt(1), args.valueAt(2));
	}
}
