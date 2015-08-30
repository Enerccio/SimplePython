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

import me.enerccio.sp.errors.IndexError;
import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.Tags;
import me.enerccio.sp.types.base.NumberObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Coerce;

public class FrameTypeObject extends TypeObject {
	private static final long serialVersionUID = -5475655736058235162L;
	public static final String FRAME_CALL = "frame";

	@Override
	public String getTypeIdentificator() {
		return "frame";
	}
	
	@Override
	public byte getTag() {
		return Tags.FRAME_TYPE;
	}

	@Override
	public PythonObject call(TupleObject args, KwArgs kwargs) {
		if (args.len() > 1)
			throw new TypeError("frame(): requiring 0 or 1 arguments, got "
					+ args.len());
		if (kwargs != null)
			kwargs.checkEmpty("frame");
		PythonInterpreter i = PythonInterpreter.interpreter.get();
		if (args.len() == 0)
			return NumberObject.valueOf(i.currentFrame.size());
		int nthFromTop = Coerce.argument(args, 0, "call", int.class);
		if (nthFromTop < 0)
			throw new IndexError("n must be positive");
		int idx = i.currentFrame.size() - (nthFromTop + 1);
		if (idx < 0 || idx >= i.currentFrame.size())
			throw new IndexError("n out of bounds");
		return i.currentFrame.get(idx);
	}

}
