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
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.enerccio.sp.types.PythonObject;

public abstract class AbstractPySerializer implements PySerializer, PySerializerDataSource {

	private DataOutputStream writer;
	private Set<Long> serialized;
	private Map<String, Long> stringCache;
	private Map<Object, Long> objectCache;
	private long sid;
	private long oid;
	
	public static int HEADER = 0xFACEDACE;
	public static int VERSION = 0x0000001;
	
	@Override
	public void initialiteSerialization() throws Exception {
		writer = new DataOutputStream(getOutput());
		serialized = new HashSet<Long>();
		stringCache = new HashMap<String, Long>();
		objectCache = new HashMap<Object, Long>();
		sid = 0;
		oid = 0;
		serialize(HEADER);
		serialize(VERSION);
	}

	@Override
	public void finishSerialization() throws Exception {
		writer.flush();
		writer.close();
	}
	
	@Override
	public void serialize(PythonObject object) {
		if (object == null){
			serialize(false);
		} else {
			serialize(object.getTag());
			serialize(true);
			if (serialized.contains(object.linkName)){
				serialize(true);
				serialize(object.linkName);
			} else {
				serialized.add(object.linkName);
				object.serializeInnerState((PySerializer)this);
			}
		}
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
	
	public static byte STRING_CACHED = 2;
	public static byte CACHE_STRING = 1;
	public static byte NO_CACHE_STRING = 0;
	public static byte STRING_NULL = -1;

	@Override
	public void serialize(String object) {
		if (object == null){
			serialize(STRING_NULL);
		} else if (stringCache.containsKey(object)){
			serialize(STRING_CACHED);
			serialize(stringCache.get(object));
		} else {	
			byte[] bd = object.getBytes();
			
			if (bd.length < 8){
				serialize(NO_CACHE_STRING);
			} else {
				serialize(CACHE_STRING);
				serialize(sid);
				stringCache.put(object, sid++);
			}
			serialize(bd);
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
	public void serializeJava(Object o){
		try {
			if (objectCache.containsKey(o)){
				serialize(true);
				serialize(objectCache.get(o));
			} else {
				serialize(false);
				serialize(oid);
				objectCache.put(o, oid++);
				ObjectOutputStream owriter = new ObjectOutputStream(getOutput());
				owriter.writeObject(o);
			}
		} catch (Exception e){
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
