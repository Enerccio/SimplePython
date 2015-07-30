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
import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

/**
 * Represents python methods
 * @author Enerccio
 *
 */
public class UserMethodObject extends CallableObject {
	private static final long serialVersionUID = 6184279154550720464L;
	public static final String SELF = "__self__";
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
	 * Calls this method. Will insert onto frame stack and returns None.
	 * @param args
	 * @return
	 */
	public PythonObject call(TupleObject args, KwArgs kwargs) {
		PythonObject callable = get(UserMethodObject.FUNC, null);
		PythonObject caller = get(UserMethodObject.SELF, null);
		PythonObject accessor = fields.get(ACCESSOR) == null ? null : fields.get(ACCESSOR).object;
		
		if (accessor == null)
			accessor = NoneObject.NONE;
		PythonInterpret.interpret.get().currentContext.add(accessor);
		
		TupleObject aargs = new TupleObject(Utils.pushLeft(caller, args.getObjects().clone()));
		
		if (callable instanceof UserFunctionObject){
			UserFunctionObject c = (UserFunctionObject)callable;
			PythonInterpret.interpret.get().invoke(c, kwargs, aargs);
			PythonInterpret.interpret.get().currentFrame.getLast().pushed_context = true;
		} else {
			JavaFunctionObject c = (JavaFunctionObject)callable;
			PythonInterpret.interpret.get().invoke(c, kwargs, aargs);
			PythonInterpret.interpret.get().currentContext.pop();
		}
		
		return NoneObject.NONE; // returns immediately
	}

	@Override
	public PythonObject set(String key, PythonObject localContext, PythonObject value) {
		if (key.equals(SELF) || key.equals(FUNC) || key.equals(ACCESSOR))
			throw Utils.throwException("AttributeError", "'" + 
					Utils.run("str", Utils.run("type", this)) + "' object attribute '" + key + "' is read only");
		return super.set(key, localContext, value);
	}

	@Override
	protected String doToString() {
		return "<method " + fields.get(FUNC).object + " of object " + fields.get(SELF).object + ">"; // TODO
	}

	@Override
	public boolean truthValue() {
		return true;
	}
}
