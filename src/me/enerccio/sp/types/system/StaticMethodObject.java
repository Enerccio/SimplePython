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
package me.enerccio.sp.types.system;

import java.util.Map;
import java.util.Set;

import me.enerccio.sp.errors.AttributeError;
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.serialization.PySerializer;
import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.Tags;
import me.enerccio.sp.types.callables.CallableObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

/**
 * StaticMethod python object
 * 
 * @author Enerccio
 *
 */
public class StaticMethodObject extends CallableObject {
	private static final long serialVersionUID = -7257861263236747558L;
	public static final String __FUNC__ = "__FUNC__";

	public StaticMethodObject() {

	}

	@Override
	public byte getTag() {
		return Tags.STATICM;
	}

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	public PythonObject set(String key, PythonObject localContext,
			PythonObject value) {
		if (!fields.containsKey(key))
			throw new AttributeError("'"
					+ Utils.run("str", Utils.run("typename", this))
					+ "' object has no attribute '" + key + "'");
		throw new AttributeError("'"
				+ Utils.run("str", Utils.run("typename", this))
				+ "' object attribute '" + key + "' is read only");
	}

	@Override
	public void create(String key, AccessRestrictions restrictions,
			PythonObject localContext) {

	}

	@Override
	protected String doToString() {
		return "<static method of " + fields.get(__FUNC__).object.toString()
				+ ">";
	}

	@Override
	public Set<String> getGenHandleNames() {
		return PythonObject.sfields.keySet();
	}

	@Override
	protected Map<String, JavaMethodObject> getGenHandles() {
		return PythonObject.sfields;
	}

	@Override
	public PythonObject call(TupleObject args, KwArgs kwargs) {
		return ((CallableObject) fields.get(__FUNC__).object)
				.call(args, kwargs);
	}

	@Override
	protected void serializeDirectState(PySerializer pySerializer) {

	}
}
