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
package me.enerccio.sp.types.pointer;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.utils.Utils;

/**
 * PointerObject is wrapped java object with methods accessibe all according to factory which created it
 * @author Enerccio
 *
 */
public class PointerObject extends PythonObject {
	private static final long serialVersionUID = 25L;
	
	public PointerObject(Object o){
		pointed = o;
	}
	
	private Object pointed;

	@Override
	public boolean truthValue() {
		return true;
	}

	public Object getObject() {
		return pointed;
	}
	
	@Override
	public int getId(){
		if (pointed == null)
			return 0;
		return pointed.hashCode();
	}

	@Override
	public PythonObject set(String key, PythonObject localContext,
			PythonObject value) {
		if (!fields.containsKey(key))
			throw Utils.throwException("AttributeError", "'" + 
					Utils.run("str", Utils.run("typename", this)) + "' object has no attribute '" + key + "'");
		throw Utils.throwException("AttributeError", "'" + 
				Utils.run("str", Utils.run("typename", this)) + "' object attribute '" + key + "' is read only");
	}

	@Override
	public void create(String key, AccessRestrictions restrictions, PythonObject localContext) {
		
	}

	@Override
	protected String doToString() {
		return "<pointer object to " + pointed.toString() + ">";
	}
}
