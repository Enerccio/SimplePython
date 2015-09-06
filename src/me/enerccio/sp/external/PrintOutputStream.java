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
package me.enerccio.sp.external;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;

import me.enerccio.sp.errors.IOError;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.serialization.PySerializer;
import me.enerccio.sp.types.pointer.WrapAnnotationFactory.WrapMethod;

public class PrintOutputStream implements Externalizable {

	private boolean err;

	public PrintOutputStream(boolean err) {
		this.err = err;
	}

	@WrapMethod
	public synchronized void write(String data) {
		@SuppressWarnings("resource")
		OutputStream os = err ? PythonRuntime.runtime.getErr()
				: PythonRuntime.runtime.getOut();
		try {
			os.write(data.getBytes());
		} catch (IOException e) {
			throw new IOError("failed to write to stream", e);
		}
	}

	@Override
	public String toString() {
		return "std" + (err ? "err" : "out") + " stream";
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		PySerializer s = PythonRuntime.activeSerializer;
		s.serialize(err);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		// TODO Auto-generated method stub

	}
}
