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
package me.enerccio.sp.types.base;

import me.enerccio.sp.compiler.Bytecode;
import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.CallableObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class CustomBytecode extends PythonBytecode {
	private static final long serialVersionUID = 388228361390637623L;
	public static final String OPERATION = "__op__";
	
	@Override
	public void newObject() {
		super.newObject();
		
		try {
			Utils.putPublic(this, CallableObject.__CALL__, new JavaMethodObject(this, this.getClass().getMethod("call", 
					new Class<?>[]{TupleObject.class}), true));
			Utils.putPublic(this, OPERATION, NoneObject.NONE);
		} catch (NoSuchMethodException e){
			// will not happen
		}
	};
	
	@Override
	public Bytecode getOpcode() {
		return Bytecode.CUSTOM;
	}

	public PythonObject call(TupleObject args){
		return PythonInterpret.interpret.get().execute(false, get(OPERATION, this), args.getObjects());
	}
}
