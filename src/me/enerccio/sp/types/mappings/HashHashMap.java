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
package me.enerccio.sp.types.mappings;

import java.util.HashMap;

import me.enerccio.sp.types.PythonObject;

/**
 * HashMap for the PythonProxy->PythoObject that looks like PythonPython->PythonPython
 * @author Enerccio
 *
 * @param <T>
 */
public class HashHashMap<T> extends HashMap<PythonProxy, T> {
	private static final long serialVersionUID = -6880977211393857008L;

	@Override
	public synchronized T get(Object key) {
		if (key instanceof PythonProxy)
			return super.get(key);
		return super.get(new PythonProxy((PythonObject) key));
	}

	@Override
	public synchronized boolean containsKey(Object key) {
		if (key instanceof PythonProxy)
			return super.containsKey(key);
		return super.containsKey(new PythonProxy((PythonObject) key));
	}

	public synchronized T put(PythonObject key, T value) {
		return super.put(new PythonProxy(key), value);
	}

	@Override
	public synchronized T remove(Object key) {
		if (key instanceof PythonProxy)
			return super.remove(key);
		return super.remove(new PythonProxy((PythonObject) key));
	}
	
}
