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

import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.ClassInstanceObject;
import me.enerccio.sp.types.base.ComplexObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.base.RealObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

/**
 * float()
 * @author Enerccio
 *
 */
public class RealTypeObject extends TypeObject {
	private static final long serialVersionUID = -8799583211649909780L;
	public static final String REAL_CALL = "float";
	
	@Override
	public String getTypeIdentificator() {
		return "float";
	}

	@Override
	public PythonObject call(TupleObject args, KwArgs kwargs){
		if (kwargs != null)
			kwargs.notExpectingKWArgs();	// Throws exception if there is kwarg defined 
		if (args.len() != 1)
			throw Utils.throwException("TypeError", "real(): Incorrect number of parameters");
		
		PythonObject a = args.valueAt(0);
		
		if (a instanceof IntObject)
			return new RealObject(((IntObject) a).getJavaFloat());
		if (a instanceof RealObject)
			return a;
		if (a instanceof ComplexObject)
			return new RealObject(((ComplexObject)a).getJavaFloat());
		if (a instanceof ClassInstanceObject){
			ClassInstanceObject c = (ClassInstanceObject)a;
			int cfc = PythonInterpreter.interpret.get().currentFrame.size();
			Utils.run("getattr", c, new StringObject("__int__"));
			PythonObject attr = PythonInterpreter.interpret.get().executeAll(cfc);
			PythonInterpreter.interpret.get().execute(false, attr, null);
			try {
				return new RealObject(((IntObject)PythonInterpreter.interpret.get().executeAll(cfc)).intValue());
			} catch (ClassCastException e){
				throw Utils.throwException("TypeError", "real(): Incorrect type of parameter");
			}
		}
		
		throw Utils.throwException("TypeError", "real(): Incorrect type of parameter");
	}
}
