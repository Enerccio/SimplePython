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

import java.io.Serializable;

import me.enerccio.sp.types.PythonObject;

/**
 * PythonProxy which delegated hashCode to the PythonObject
 * 
 * @author Enerccio
 *
 */
public class PythonProxy implements Serializable {
	private static final long serialVersionUID = 5305512089616516954L;
	public PythonObject o;

	public PythonProxy(PythonObject key) {
		o = key;
	}

	@Override
	public int hashCode() {
		return o.getId();
	}

	@Override
	public boolean equals(Object p) {
		return ((PythonProxy) p).o.equals(o);
	}

	@Override
	public String toString() {
		return o.toString();
	}
}