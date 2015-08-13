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
import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.NumberObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;

/**
 * int()
 * @author Enerccio
 *
 */
public class IntTypeObject extends TypeObject {
	private static final long serialVersionUID = -4178003762513900453L;
	public static final String INT_CALL = "int";

	public IntTypeObject(){
		
	}

	@Override
	public String getTypeIdentificator() {
		return "int";
	}


	@Override
	public PythonObject call(TupleObject args, KwArgs kwargs){
		PythonObject base = null;
		if (kwargs != null){
			base = kwargs.consume("base");
			kwargs.checkEmpty("int");	
		}
		if (args.len() < 1)
			return NumberObject.valueOf(0l);
		if (args.len() > 2)
			throw new TypeError("int() takes at most 2 arguments");
		PythonObject obj = args.get(0);
		if (args.len() == 2) {
			if (base != null)
				throw new TypeError("int() got duplicate value for argument 'base'");
			base = args.get(1);
		}
		if (base != null) {
			if (!NumberObject.isInteger(base))
				throw new TypeError("int() base argument must be integer");
			if (!(obj instanceof StringObject))
				throw new TypeError("int() can't convert non-string with explicit base");
			return NumberObject.valueOf(Integer.valueOf(obj.toString(), ((NumberObject)base).intValue()));
		}
		
		if (obj instanceof StringObject)
			return NumberObject.valueOf(Integer.valueOf(obj.toString()));
		if (obj instanceof NumberObject)
			return NumberObject.valueOf(((NumberObject)obj).intValue());
		PythonObject __int__ = obj.get(NumberObject.__INT__, null);
		if (__int__ != null) {
			int cfc = PythonInterpreter.interpreter.get().currentFrame.size();
			PythonInterpreter.interpreter.get().execute(false, __int__, null);
			return PythonInterpreter.interpreter.get().executeAll(cfc);
		}
		
		throw new TypeError("int() can't convert " + obj.toString() + " to int");
	}
}
