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
package me.enerccio.sp.types.types;

/**
 * javamethod()
 * 
 * @author Enerccio
 *
 */
public class JavaCallableTypeObject extends TypeObject {
	private static final long serialVersionUID = 7480637583186194657L;
	private static TypeObject singleton = null;
	public static final String JAVACALLABLE_CALL = "javamethod";

	public JavaCallableTypeObject() {
		if (singleton != null)
			throw new RuntimeException("Creating 2nd instance of singleton!");
		singleton = this;
	}

	public static TypeObject get() {
		if (singleton == null)
			singleton = new JavaCallableTypeObject();
		return singleton;
	}

	@Override
	public String getTypeIdentificator() {
		return "javamethod";
	}

}
