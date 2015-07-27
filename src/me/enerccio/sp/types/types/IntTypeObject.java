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
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

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
	public PythonObject call(TupleObject args) {
		PythonObject arg1 = null, arg2 = null;
		
		if (args.len() == 0){
			arg1 = IntObject.valueOf(0);
		} else if (args.len() == 1){
			arg1 = args.getObjects()[0];
		} else if (args.len() == 2){
			arg1 = args.getObjects()[0];
			arg2 = args.getObjects()[1];
		}
		
		if (arg2 == null)
			return PythonInterpret.interpret.get().execute(false, Utils.get(arg1, IntObject.__INT__));
		else if (arg1 != null && arg2 != null){
			try {
				PythonObject strArg = Utils.run(StringTypeObject.STRING_CALL, arg2);
				PythonObject intArg = Utils.run(INT_CALL, arg2);
				return IntObject.valueOf(Integer.parseInt(((StringObject)strArg).getString(), ((IntObject)intArg).intValue()));
			} catch (ClassCastException e){
				throw Utils.throwException("TypeError", "int(): incorrect type for function call: " + e.getMessage());
			}
		}
		
		throw Utils.throwException("TypeError", "int(): Incorrect number of parameters");
	}

}
