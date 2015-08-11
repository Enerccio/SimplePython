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

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.JavaMethodObject;

/**
 * Represents slice object. Can be created via slice() function
 * @author Enerccio
 *
 */
public class SliceObject extends PythonObject {
	private static final long serialVersionUID = 5763751093225639862L;
	public static final String START_ACCESSOR = "start";
	public static final String STOP_ACCESSOR = "stop";
	public static final String STEP_ACCESSOR = "step";
	
	public SliceObject(PythonObject start, PythonObject end, PythonObject step){
		fields.put(START_ACCESSOR, new AugumentedPythonObject(start, AccessRestrictions.PUBLIC));
		fields.put(STOP_ACCESSOR, new AugumentedPythonObject(end, AccessRestrictions.PUBLIC));
		fields.put(STEP_ACCESSOR, new AugumentedPythonObject(step, AccessRestrictions.PUBLIC));
	}

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	protected String doToString() {
		return "<slice at 0x" + Integer.toHexString(hashCode()) + ">";
	}

	@Override
	public Set<String> getGenHandleNames() {
		return PythonObject.sfields.keySet();
	}

	@Override
	protected Map<String, JavaMethodObject> getGenHandles() {
		return PythonObject.sfields;
	}
}
