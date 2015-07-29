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

import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.types.sequences.XRangeObject;
import me.enerccio.sp.utils.Utils;

/**
 * xrange type object. Used as type for xrange objects and to construct new xrange objects.
 */
public class XRangeTypeObject extends TypeObject {
	private static final long serialVersionUID = -3717716515678990374L;
	public static final String XRANGE_CALL = "xrange";

	@Override
	public String getTypeIdentificator() {
		return "xrange";
	}

	@Override
	public PythonObject call(TupleObject args, KwArgs kwargs){
		if (kwargs != null)
			kwargs.notExpectingKWArgs();	// Throws exception if there is kwarg defined 
		if ((args.len() < 1) || (args.len() > 3))
			throw Utils.throwException("TypeError", "xrange() requires 1-3 int arguments");
		
		int start = ((IntObject)args.valueAt(0)).intValue();
		int end = args.len() < 2 ? 0 : ((IntObject)args.valueAt(1)).intValue();
		int step = args.len() < 3 ? 1 : ((IntObject)args.valueAt(2)).intValue();
		if (args.len() < 2) {
			end = start;
			start = 0;
		}
		if (start > end)
			end = start;
		else if (step > start + end)
			end = step + start;
		if (step == 0)
			throw Utils.throwException("TypeError", "xrange() arg 3 must not be zero");
		
		XRangeObject rv = new XRangeObject(start, end, step);
		rv.newObject();
		return rv;
	}
}
