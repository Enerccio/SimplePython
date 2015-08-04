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
package me.enerccio.sp.types.callables;

import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

/**
 * Python Function bound to certain type. This is here because of the private/public shenanigans and this bound handle
 * will set up correct context for bound objects
 * @author Enerccio
 *
 */
public class BoundHandleObject extends PythonObject {

	private static final long serialVersionUID = 6184279154550720464L;
	public static final String FUNC = "__func__";
	public static final String ACCESSOR = "__access__";
	
	@Override
	public void newObject() {
		super.newObject();
		
		try {
			Utils.putPublic(this, CallableObject.__CALL__, new JavaMethodObject(this, "call")); 
		} catch (NoSuchMethodException e){
			// will not happen
		}
	};
	
	/**
	 * Calls this function. Will insert onto frame stack and returns None.
	 * @param args
	 * @return
	 */
	public PythonObject call(TupleObject args, KwArgs kwargs) {
		PythonObject callable = get(UserMethodObject.FUNC, null);
		PythonObject accessor = fields.get(ACCESSOR) == null ? null : fields.get(ACCESSOR).object;
		
		if (accessor == null)
			accessor = NoneObject.NONE;
		
		TupleObject aargs = args;
		
		if (callable instanceof UserFunctionObject){
			UserFunctionObject c = (UserFunctionObject)callable;
			PythonInterpreter.interpreter.get().invoke(c, kwargs, aargs);
			PythonInterpreter.interpreter.get().currentFrame.getLast().localContext = accessor;
		} else {
			JavaFunctionObject c = (JavaFunctionObject)callable;
			PythonInterpreter.interpreter.get().invoke(c, kwargs, aargs);
		}
		
		return NoneObject.NONE; // returns immediately
	}

	@Override
	public PythonObject set(String key, PythonObject localContext,
			PythonObject value) {
		if (key.equals(FUNC) || key.equals(ACCESSOR))
			throw Utils.throwException("AttributeError", "'" + 
					Utils.run("str", Utils.run("typename", this)) + "' object attribute '" + key + "' is read only");
		return super.set(key, localContext, value);
	}

	@Override
	protected String doToString() {
		return "<bound-function " + fields.get(FUNC).object + " of type " + fields.get(ACCESSOR).object + ">"; // TODO
	}

	@Override
	public boolean truthValue() {
		return true;
	}
	
}
