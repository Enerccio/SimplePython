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

public interface PySerializer {
	
	void initialiteSerialization() throws Exception;
	void finishSerialization() throws Exception;

	void serialize(PythonObject object);
	
	void serialize(boolean object);
	void serialize(int object);
	void serialize(byte object);
	void serialize(String object);
	void serialize(long object);
	void serialize(double object);
	void serialize(float object);
	void serialize(char object);
	void serialize(short object);
	void serialize(byte[] object);
	
	void serializeJava(Object o);
	
	
}
