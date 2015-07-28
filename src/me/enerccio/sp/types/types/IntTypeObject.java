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
	public PythonObject call(TupleObject args) {
		PythonObject arg1 = null, arg2 = null;
		
		if (args.len() == 0){
			return IntObject.valueOf(0);
		} else if (args.len() == 1){
			arg1 = args.getObjects()[0];
		} else if (args.len() == 2){
			arg1 = args.getObjects()[0];
			arg2 = args.getObjects()[1];
		} else
			throw Utils.throwException("TypeError", "int(): Incorrect number of parameters");
		
		PythonObject o1 = null, o2 = null;
		int cfc = PythonInterpret.interpret.get().currentFrame.size();
		
		if (arg2 == null){
			Utils.run("getattr", arg1, new StringObject("__int__"));
			PythonObject attr = PythonInterpret.interpret.get().executeAll(cfc);
			PythonInterpret.interpret.get().execute(false, attr);
			return PythonInterpret.interpret.get().executeAll(cfc);
		} else {
			Utils.run("getattr", arg2, new StringObject("__int__"));
			PythonObject attr = PythonInterpret.interpret.get().executeAll(cfc);
			PythonInterpret.interpret.get().execute(false, attr);
			o2 = PythonInterpret.interpret.get().executeAll(cfc);
		}
		
		if (arg2 != null){
			Utils.run("str", arg1);
			o1 = PythonInterpret.interpret.get().executeAll(cfc);
		}
		
		try {
			IntObject i = (IntObject)o2;	
			StringObject s = (StringObject)o1;
			
			return IntObject.valueOf(Integer.parseInt(s.getString(), i.intValue()));
			
		} catch (ClassCastException e){
			throw Utils.throwException("TypeError", "int(): incorrect type for function call: " + e.getMessage());
		}
	}

}
