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

import java.util.Map;
import java.util.Set;

import me.enerccio.sp.errors.AttributeError;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.serialization.PySerializer;
import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.Tags;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.types.NoneTypeObject;
import me.enerccio.sp.types.types.TypeObject;
import me.enerccio.sp.utils.Utils;

/**
 * Singleton None object
 * 
 * @author Enerccio
 *
 */
public class NoneObject extends PythonObject {
	private static final long serialVersionUID = 2L;

	public static final NoneObject NONE = new NoneObject();
	public static final TypeObject TYPE = new NoneTypeObject();

	public NoneObject() {
		super(false);
	}

	@Override
	public byte getTag() {
		return Tags.NONE;
	}

	@Override
	public void newObject() {
		if (PythonRuntime.NONE_TYPE != null)
			super.newObject();
	}

	@Override
	public boolean truthValue() {
		return false;
	}

	@Override
	public ClassObject getType() {
		return TYPE;
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
	public Set<String> getGenHandleNames() {
		return PythonObject.sfields.keySet();
	}

	@Override
	protected Map<String, JavaMethodObject> getGenHandles() {
		return PythonObject.sfields;
	}

	@Override
	public void create(String key, AccessRestrictions restrictions,
			PythonObject localContext) {

	}

	@Override
	protected String doToString() {
		return "None";
	}

	@Override
	protected void serializeDirectState(PySerializer pySerializer) {

	}
}
