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
package me.enerccio.sp.types;

/**
 * Holds the python object along with access restrictions and the defining class
 * @author Enerccio
 *
 */
public class AugumentedPythonObject {
	
	public AugumentedPythonObject(PythonObject object,
			AccessRestrictions restrictions, PythonObject owner) {
		this.object = object;
		this.restrictions = restrictions;
		this.owner = owner;
	}
	
	public AugumentedPythonObject(PythonObject object,
			AccessRestrictions restrictions) {
		this.object = object;
		this.restrictions = restrictions;
		this.owner = null;
	}
	
	public volatile PythonObject object;
	public final AccessRestrictions restrictions;
	public final PythonObject owner;
	
	@Override
	public String toString() {
		return "AugumentedPythonObject [object=" + object + ", restrictions="
				+ restrictions + ", owner=" + owner + "]";
	} 
	
}
