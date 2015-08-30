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

import me.enerccio.sp.serialization.PySerializer;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.JavaMethodObject;

/**
 * Ellipsis object
 * 
 * @author Enerccio
 *
 */
public class EllipsisObject extends PythonObject {
	private static final long serialVersionUID = 70L;

	public static final EllipsisObject ELLIPSIS = new EllipsisObject();

	private EllipsisObject() {
		super(false);
	}

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	protected String doToString() {
		return "...";
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
	protected void serializeDirectState(PySerializer pySerializer) {
		
	}
}
