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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

import me.enerccio.sp.types.pointer.WrapAnnotationFactory.WrapMethod;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.utils.Utils;

public class FileStream {

	private RandomAccessFile file;
	private String encoding;
	private boolean bytestream;
	public FileStream(String file, String mode){
		try {
			init(file, mode);
		} catch (Exception e){
			throw Utils.throwException("IOError", "failed to open file " + file, e);
		}
	}
	
	private void init(String file, String mode) throws Exception {
		String m = mode.substring(0, 1);
		if (mode.endsWith("b")){
			bytestream = true;
		} else {
			encoding = "utf-8";
		}
		this.file = new RandomAccessFile(file, m);
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
			throw Utils.throwException("IOError", "failed to close resource", e);
		}
	}
	
	@WrapMethod
	public FileDescriptor fileno(){
		try {
			return file.getFD();
		} catch (IOException e) {
			throw Utils.throwException("IOError", "io error", e);
		}
	}
	
	@WrapMethod
	public boolean isatty(){
		return false;
	}
	
	@WrapMethod
	public StringObject read(int count){
		byte[] data = new byte[count];
		try {
			file.read(data);
		} catch (IOException e) {
			throw Utils.throwException("IOError", "read failed", e);
		}
		if (bytestream)
			return new StringObject(new String(data));
		else
			try {
				return new StringObject(new String(data, encoding));
			} catch (UnsupportedEncodingException e) {
				throw Utils.throwException("IOError", "encoding error", e);
			}
	}
	
	@WrapMethod
	public void seek(int pos, int whence) {
		try {
			if (whence == 0){
				file.seek(pos);
			} else if (whence == 1){
				file.seek(file.getFilePointer() + pos);
			} else {
				file.seek(file.length() + pos);
			}
		} catch (IOException e) {
			throw Utils.throwException("IOError", "seek failed", e);
		}
	}
	
	@WrapMethod
	public long tell(){
		try {
			return file.getFilePointer();
		} catch (IOException e) {
			throw Utils.throwException("IOError", "tell failed", e);
		}
	}
	
	@WrapMethod
	public void write(StringObject o){
		try {
			if (bytestream)
				file.write(o.value.getBytes());
			else
				try {
					file.write(o.value.getBytes(encoding));
				} catch (UnsupportedEncodingException e) {
					throw Utils.throwException("IOError", "encoding error", e);
				}
		} catch (IOException e) {
			throw Utils.throwException("IOError", "write failed", e);
		}
	}
}
