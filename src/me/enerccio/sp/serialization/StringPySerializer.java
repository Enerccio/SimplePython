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
package me.enerccio.sp.serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class StringPySerializer extends AbstractPySerializer {

	private OutputStream out;
	private ByteArrayOutputStream bos;

	public StringPySerializer() throws IOException {
		out = bos = new ByteArrayOutputStream();
	}

	@Override
	public OutputStream getOutput() {
		return out;
	}

	public String getString() throws Exception {
		return getString("utf-8");
	}

	public String getString(String encoding) throws Exception {
		return bos.toString(encoding);
	}
}
