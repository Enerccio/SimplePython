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
package me.enerccio.sp.runtime;

import java.io.IOException;
import java.io.InputStream;

import org.jcp.xml.dsig.internal.dom.Utils;

public class PythonClassLoader extends ClassLoader {

	public PythonClassLoader(ClassLoader parent) {
		super(parent);
	}

	public Class<?> load(String binaryName, InputStream inputData)
			throws IOException {
		byte[] array = Utils.readBytesFromStream(inputData);
		return defineClass(binaryName, array, 0, array.length);
	}
}
