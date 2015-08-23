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

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

import me.enerccio.sp.errors.IOError;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.sandbox.SecureAction;
import me.enerccio.sp.types.pointer.WrapAnnotationFactory.WrapMethod;
import me.enerccio.sp.types.sequences.StringObject;

public class FileStream {

	private RandomAccessFile file;
	private String encoding;
	private boolean bytestream;

	public FileStream(String file, String mode) {
		PythonRuntime.runtime.checkSandboxAction("filestream",
				SecureAction.OPEN_FILE, file, mode);

		try {
			init(file, mode);
		} catch (Exception e) {
			throw new IOError("failed to open file " + file, e);
		}
	}

	private void init(String file, String mode) throws Exception {
		String m = mode.substring(0, 1);
		if (mode.endsWith("b")) {
			bytestream = true;
		} else {
			encoding = "utf-8";
		}
		try {
			this.file = new RandomAccessFile(file, m);
		} catch (FileNotFoundException e) {
			throw new IOError("open(): no such file or directory: '" + file
					+ "'");
		}
	}

	@WrapMethod
	public String encoding() {
		return encoding;
	}

	@WrapMethod
	public void close() {
		try {
			file.close();
		} catch (IOException e) {
			throw new IOError("failed to close resource", e);
		}
	}

	@WrapMethod
	public FileDescriptor fileno() {
		try {
			return file.getFD();
		} catch (IOException e) {
			throw new IOError("io error", e);
		}
	}

	@WrapMethod
	public boolean isatty() {
		return false;
	}

	@WrapMethod
	public StringObject read(int count) {
		byte[] data = new byte[count];
		int rc;
		try {
			rc = file.read(data);
		} catch (IOException e) {
			throw new IOError("read failed", e);
		}

		data = truncate(data, rc);
		if (data.length == 0)
			return new StringObject("");

		if (bytestream)
			return new StringObject(new String(data));
		else
			try {
				return new StringObject(new String(data, encoding));
			} catch (UnsupportedEncodingException e) {
				throw new IOError("encoding error", e);
			}
	}

	private byte[] truncate(byte[] data, int rc) {
		if (rc == -1)
			return new byte[0];

		byte[] b = new byte[rc];
		for (int i = 0; i < rc; i++)
			b[i] = data[i];
		return b;
	}

	@WrapMethod
	public void seek(int pos, int whence) {
		try {
			if (whence == 0) {
				file.seek(pos);
			} else if (whence == 1) {
				file.seek(file.getFilePointer() + pos);
			} else {
				file.seek(file.length() + pos);
			}
		} catch (IOException e) {
			throw new IOError("seek failed", e);
		}
	}

	@WrapMethod
	public long tell() {
		try {
			return file.getFilePointer();
		} catch (IOException e) {
			throw new IOError("tell failed", e);
		}
	}

	@WrapMethod
	public void write(StringObject o) {
		try {
			if (bytestream)
				file.write(o.value.getBytes());
			else
				try {
					file.write(o.value.getBytes(encoding));
				} catch (UnsupportedEncodingException e) {
					throw new IOError("encoding error", e);
				}
		} catch (IOException e) {
			throw new IOError("write failed", e);
		}
	}
}
