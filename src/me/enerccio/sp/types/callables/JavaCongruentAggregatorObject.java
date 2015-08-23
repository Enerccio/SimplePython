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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.PointerMethodIncompatibleException;

/**
 * Aggreagtes methods with same name of the java class into single callable,
 * that will call applicable methods based on the arguments used.
 * 
 * @author Enerccio
 *
 */
public class JavaCongruentAggregatorObject extends CallableObject {
	private static final long serialVersionUID = -8330175039684193277L;

	private String name;
	public Set<JavaMethodObject> methods = Collections
			.synchronizedSet(new HashSet<JavaMethodObject>());

	public JavaCongruentAggregatorObject(String n) {
		name = n;
	}

	@Override
	public PythonObject call(TupleObject args, KwArgs kwargs) {
		for (JavaMethodObject mo : methods)
			try {
				return mo.doCall(args, kwargs);
			} catch (PointerMethodIncompatibleException e) {
				if (methods.size() == 1)
					throw new TypeError(e.getMessage(), e);
			}
		throw new TypeError(name + "(): no applicable method found");
	}

	@Override
	protected String doToString() {
		return "<java-methods " + name + ">";
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
