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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.serialization.PySerializer;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.Tags;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

/**
 * Class Instance object. Created by ClassObjects.
 * 
 * @author Enerccio
 *
 */
public class ClassInstanceObject extends PythonObject {
	private static final long serialVersionUID = -4687801735710617861L;
	public static final String __INIT__ = "__init__";
	public static final String __STR__ = "__str__";
	public static final String __GETATTR__ = "__getattr__";
	public static final String __SETATTR__ = "__setattr__";
	public static final String __HASH__ = "__hash__";
	public static final String __GETATTRIBUTE__ = "__getattribute__";

	@Override
	public byte getTag() {
		return Tags.INSTANCE;
	}

	public ClassInstanceObject() {
		super(false);
	}

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	protected String doToString() {
		return "<instance of "
				+ fields.get(ClassObject.__CLASS__).object.toString()
				+ " at 0x" + Long.toHexString(getId()) + ">";
	}

	private static JavaMethodObject pyHash;
	static {
		try {
			pyHash = new JavaMethodObject(ClassInstanceObject.class, "pyHash");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Should be only called when object is created, not when any other class
	// is!
	public void initObject() {
		Utils.putPublic(this, __HASH__, pyHash.cloneWithThis(this));
	}

	public NumberObject pyHash(TupleObject args, KwArgs kw) {
		if (args.len() != 0)
			throw new TypeError("__hash__(): requires 0 parameters");
		if (kw != null)
			kw.notExpectingKWArgs();
		return NumberObject.valueOf(getId());
	}

	@Override
	public Set<String> getGenHandleNames() {
		return new HashSet<String>();
	}

	@Override
	protected Map<String, JavaMethodObject> getGenHandles() {
		return new HashMap<String, JavaMethodObject>();
	}

	@Override
	protected void serializeDirectState(PySerializer pySerializer) {

	}
}
