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

import me.enerccio.sp.errors.AttributeError;
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.Tags;
import me.enerccio.sp.types.base.BoolObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.properties.MethodPropertyObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

/**
 * Base type object. All types are ClassObject types as well.
 * 
 * @author Enerccio
 *
 */
public abstract class TypeObject extends ClassObject {
	private static final long serialVersionUID = 5891250487396458462L;

	@Override
	public void newObject() {
		if (PythonRuntime.NONE_TYPE != null) {
			super.newObject();

			try {
				Utils.putPublic(
						this,
						"__name__",
						new MethodPropertyObject("__name__", JavaMethodObject
								.noArgMethod(this, "getTypeIdentificator")));
			} catch (NoSuchMethodException | SecurityException e) {
				throw new RuntimeException("kurva", e);
			}
		}
	}

	public abstract String getTypeIdentificator();

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
	public PythonObject eq(PythonObject other) {
		return BoolObject.fromBoolean(equals(other));
	}

	@Override
	public PythonObject call(TupleObject o, KwArgs varArgs) {
		return NoneObject.NONE;
	}

	@Override
	protected String doToString() {
		return "<type " + getTypeIdentificator() + ">";
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof TypeObject)
			return ((TypeObject) o).getTypeIdentificator().equals(
					getTypeIdentificator());
		return false;
	}

	@Override
	public byte getTag() {
		return Tags.TYPE_TYPE;
	}
}
