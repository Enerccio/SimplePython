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

import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.ClassInstanceObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class StringTypeObject extends TypeObject {
	private static final long serialVersionUID = 189813854164565772L;
	public static final String STRING_CALL = "str";

	@Override
	public String getTypeIdentificator() {
		return STRING_CALL;
	}

	@Override
	public PythonObject call(TupleObject args) {
		if (args.len() != 1)
			throw Utils.throwException("TypeError", "str(): incorrect number of parameters");
		
		PythonObject o = args.getObjects()[0];
		if (o instanceof ClassInstanceObject){
			int cfc = PythonInterpret.interpret.get().currentFrame.size();
			Utils.run("getattr", args.getObjects()[0], new StringObject("__str__"));
			PythonObject ret = PythonInterpret.interpret.get().executeAll(cfc);
			return PythonInterpret.interpret.get().execute(false, ret);
		} else
			return new StringObject(o.toString());
	}
}
