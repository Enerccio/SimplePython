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
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

/**
 * javainstance()
 * @author Enerccio
 *
 */
public class JavaInstanceTypeObject extends TypeObject {
	private static final long serialVersionUID = -1082259923569412321L;
	public static final String JAVA_CALL = "javainstance";

	@Override
	public String getTypeIdentificator() {
		return "javainstance";
	}

	@Override
	public PythonObject call(TupleObject args, KwArgs kwargs){
		if (PythonRuntime.runtime.isSandboxMode())
			throw Utils.throwException("SandboxViolationError", "javainstance() not allowed");
			
		if (kwargs != null)
			kwargs.notExpectingKWArgs();	// Throws exception if there is kwarg defined 
		if (args.len() < 1)
			throw Utils.throwException("TypeError", "javainstance(): requires at least 1 parameter");
		
		PythonObject clsName = args.valueAt(0);
		if (!(clsName instanceof StringObject))
			throw Utils.throwException("TypeError", "javainstance():  first argument must be str");
		
		String cls = ((StringObject)clsName).value;
		
		return PythonRuntime.runtime.getJavaClass(cls, null, kwargs, Utils.removeFirst(args.getObjects()));
	}

	
}
