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
package me.enerccio.sp.types.sequences;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.utils.Utils;

/**
 * Mutable Sequences, provides __setitem__ method
 * @author Enerccio
 *
 */
public abstract class MutableSequenceObject extends SequenceObject {
	private static final long serialVersionUID = 15L;
	public static final String __SETITEM__ = "__setitem__";

	private static Map<String, JavaMethodObject> sfields = new HashMap<String, JavaMethodObject>();
	
	static {
		try {
			sfields.putAll(SequenceObject.getSFields());
			sfields.put(__SETITEM__, new JavaMethodObject(MutableSequenceObject.class, "set", PythonObject.class, PythonObject.class));
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	protected static Map<String, JavaMethodObject> getSFields(){ return sfields; }
	@Override
	public Set<String> getGenHandleNames() {
		return sfields.keySet();
	}

	@Override
	protected Map<String, JavaMethodObject> getGenHandles() {
		return sfields;
	}
	
	@Override
	public void newObject() {
		super.newObject();
	}
	
	public abstract PythonObject set(PythonObject key, PythonObject value);
	
	
	@Override
	public int getId(){
		throw Utils.throwException("TypeError", "unhashable type '" + Utils.run("typename", this) + "'");
	}
}
