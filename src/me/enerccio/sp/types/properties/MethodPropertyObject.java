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
package me.enerccio.sp.types.properties;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class MethodPropertyObject extends PythonObject implements PropertyObject {
	private static final long serialVersionUID = 8102333533134900507L;
	private String name;
	private JavaMethodObject getter;
	private JavaMethodObject setter;
	
	public MethodPropertyObject(String name, JavaMethodObject getter){
		this(name, getter, null);
	}
	
	public MethodPropertyObject(String name, JavaMethodObject getter, JavaMethodObject setter) {
		this.name = name;
		this.getter = getter;
		this.setter = setter;
	}
	
	public MethodPropertyObject() {
		
	}

	@Override
	public void set(PythonObject set) {
		if (setter == null)
			throw Utils.throwException("TypeError", "field '" + name + "' is read-only");
		setter.call(new TupleObject(set), null);
	}

	@Override
	public PythonObject get() {
		return getter.call(new TupleObject(), null);
	}

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	protected String doToString() {
		return "<" + (setter == null ? "read-only " : " " ) + "property '" + name + "' at 0x" + Integer.toHexString(hashCode()) + ">";
	}

	public MethodPropertyObject bindTo(Object self){
		MethodPropertyObject mpo = new MethodPropertyObject();
		mpo.name = name;
		mpo.getter = getter.cloneWithThis(self);
		if (setter != null)
			mpo.setter = setter.cloneWithThis(self);
		return mpo;
	}
}
