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

import me.enerccio.sp.types.PythonObject;

public interface PyDeserializer {

	public static interface NativeResult {
		public void run(Object nativeObject);
	}
	
	void initializeSerialization() throws Exception;
	void finishDeserialization() throws Exception;
	
	public PythonObject next();
	public boolean nextBoolean();
	public byte    nextByte();
	public long    nextLong();
	public int     nextInt();
	public float   nextFloat();
	public double  nextDouble();
	public char    nextChar();
	public short   nextShort();
	public byte[]  nextBytes();
	public String  nextString();
	
	public void nextNative(NativeResult nr);
	public PythonObject getByLink(long link);
}
