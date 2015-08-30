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

import java.io.DataOutputStream;
import java.io.IOException;

public abstract class AbstractPySerializer extends BasePySerializer {

	private DataOutputStream writer;
	
	@Override
	public void initialiteSerialization() throws Exception {
		super.initialiteSerialization();
		writer = new DataOutputStream(getOutput());
		serialize(HEADER);
		serialize(VERSION);
	}

	@Override
	public void finishSerialization() throws Exception {
		super.finishSerialization();
		writer.flush();
		writer.close();
	}

	@Override
	public void serialize(boolean object) {
		try {
			writer.writeBoolean(object);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void serialize(int object) {
		try {
			writer.writeInt(object);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void serialize(byte object) {
		try {
			writer.writeByte(object);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void serialize(long object) {
		try {
			writer.writeLong(object);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void serialize(double object) {
		try {
			writer.writeDouble(object);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void serialize(float object) {
		try {
			writer.writeFloat(object);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void serialize(char object) {
		try {
			writer.writeChar(object);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void serialize(short object) {
		try {
			writer.writeShort(object);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void serialize(byte[] object){
		try {
			writer.writeInt(object.length);
			writer.write(object);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
