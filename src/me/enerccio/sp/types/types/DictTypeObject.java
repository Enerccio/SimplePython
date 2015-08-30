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
import me.enerccio.sp.types.sequences.TupleObject;

/**
 * dict()
 * 
 * @author Enerccio
 *
 */
public class DictTypeObject extends TypeObject {
	private static final long serialVersionUID = -3923399715218968304L;
	public static final String DICT_CALL = "dict";

	@Override
	public String getTypeIdentificator() {
		return "dict";
	}

	@Override
	public PythonObject call(TupleObject args, KwArgs kwargs) {
		if (args.len() > 0)
			throw new TypeError("dict(): requiring 0 arguments, got "
					+ args.len());
		return args.convertKwargs(kwargs);
	}

	@Override
	public byte getTag() {
		return Tags.DICT_TYPE;
	}
}
