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
import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.NumberObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

/**
 * float()
 * @author Enerccio
 *
 */
public class FloatTypeObject extends TypeObject {
	private static final long serialVersionUID = -8799583211649909780L;
	public static final String FLOAT_CALL = "float";
	
	@Override
	public String getTypeIdentificator() {
		return "float";
	}

	@Override
	public PythonObject call(TupleObject args, KwArgs kwargs){
		if (kwargs != null)
			kwargs.notExpectingKWArgs();	// Throws exception if there is kwarg defined 
		if (args.len() != 1)
			throw Utils.throwException("TypeError", "foat(): Incorrect number of parameters");
		
		PythonObject a = args.valueAt(0);
		
		if (a instanceof NumberObject) {
			if (PythonRuntime.USE_DOUBLE_FLOAT)
				return NumberObject.valueOf(((NumberObject)a).doubleValue());
			else
				return NumberObject.valueOf(((NumberObject)a).floatValue());
		}

		PythonObject __int__ = a.get(NumberObject.__INT__, null);
		if (__int__ != null) {
			int cfc = PythonInterpreter.interpreter.get().currentFrame.size();
			PythonInterpreter.interpreter.get().execute(false, __int__, null);
			PythonObject o = PythonInterpreter.interpreter.get().executeAll(cfc);
			if (o instanceof NumberObject) {
				if (PythonRuntime.USE_DOUBLE_FLOAT)
					return NumberObject.valueOf(((NumberObject)o).doubleValue());
				else
					return NumberObject.valueOf(((NumberObject)o).floatValue());
			} else {
				throw Utils.throwException("TypeError", "float(): __int__ did not returned number");
			}
		}

		throw Utils.throwException("TypeError", "float(): Incorrect type of parameter");
	}
}
