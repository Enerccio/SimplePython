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

import java.util.Map;
import java.util.Set;

import me.enerccio.sp.errors.AttributeError;
import me.enerccio.sp.interpret.AbstractPythonInterpreter;
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.serialization.PySerializer;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.Tags;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

/**
 * Represents python methods
 * 
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
			Utils.putPublic(this, CallableObject.__CALL__,
					new JavaMethodObject(this, "call"));
		} catch (NoSuchMethodException e) {
			// will not happen
		}
	};

	@Override
	public Set<String> getGenHandleNames() {
		return PythonObject.sfields.keySet();
	}

	@Override
	protected Map<String, JavaMethodObject> getGenHandles() {
		return PythonObject.sfields;
	}

	@Override
	public byte getTag() {
		return Tags.UMETH;
	}

	/**
	 * Calls this method. Will insert onto frame stack and returns None.
	 * 
	 * @param args
	 * @return
	 */
	@Override
	public PythonObject call(TupleObject args, KwArgs kwargs) {
		PythonObject callable = get(UserMethodObject.FUNC, null);
		PythonObject caller = get(UserMethodObject.SELF, null);
		PythonObject accessor = fields.get(ACCESSOR) == null ? null : fields
				.get(ACCESSOR).object;

		if (accessor == null)
			accessor = NoneObject.NONE;

		TupleObject aargs = new TupleObject(true, Utils.pushLeft(caller, args
				.getObjects().clone()));
		PythonObject v;

		if (callable instanceof UserFunctionObject) {
			UserFunctionObject c = (UserFunctionObject) callable;
			v = AbstractPythonInterpreter.interpreter.get().invoke(c, kwargs,
					aargs);
			AbstractPythonInterpreter.interpreter.get().currentFrame.getLast().localContext = accessor;
		} else {
			JavaFunctionObject c = (JavaFunctionObject) callable;
			v = AbstractPythonInterpreter.interpreter.get().invoke(c, kwargs,
					aargs);
		}

		return v; // returns immediately
	}

	@Override
	public PythonObject set(String key, PythonObject localContext,
			PythonObject value) {
		if (key.equals(SELF) || key.equals(FUNC) || key.equals(ACCESSOR))
			throw new AttributeError("'"
					+ Utils.run("str", Utils.run("typename", this))
					+ "' object attribute '" + key + "' is read only");
		return super.set(key, localContext, value);
	}

	@Override
	protected String doToString() {
		return "<method " + fields.get(FUNC).object + " of object "
				+ fields.get(SELF).object + ">"; // TODO
	}

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	protected void serializeDirectState(PySerializer pySerializer) {

	}
}
