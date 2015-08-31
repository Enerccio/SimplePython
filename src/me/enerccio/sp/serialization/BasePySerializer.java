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

import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.Tags;

/**
 * BasePySerializer - Provides a way to save strings, java objects and python objects, along 
 * with caching and linking. Subclasses needs to provide a data source and way of saving primitives.
 * @author Enerccio
 *
 */
public abstract class BasePySerializer implements PySerializer, PySerializationDataSource {

	@Override
	public void initializeSerialization() throws Exception {
		serialized = new HashSet<Long>();
		stringCache = new HashMap<String, Long>();
		objectCache = new HashMap<Object, Long>();
		sid = 0;
		oid = 0;
	}

	@Override
	public void finishSerialization() throws Exception {
		saveObjects();
	}
	
	private void saveObjects() throws Exception {
		List<Object> olist = new ArrayList<Object>();
		olist.addAll(objectCache.keySet());
		Collections.sort(olist, new Comparator<Object>(){

			@Override
			public int compare(Object o1, Object o2) {
				return objectCache.get(o1).compareTo(objectCache.get(o2));
			}
			
		});
		
		saveObjects(olist);
	}

	protected void saveObjects(List<Object> olist) throws Exception {
		serialize(objectCache.size());
		ObjectOutputStream owriter = new ObjectOutputStream(getOutput());
		for (Object key : olist){
			owriter.writeObject(key);
		}
	}

	protected Set<Long> serialized;
	protected Map<String, Long> stringCache;
	protected long sid;
	private   Map<Object, Long> objectCache;
	private   long oid;
	
	public static int HEADER = 0xFACEDACE;
	public static int VERSION = 0x0000001;
	
	@Override
	public void serialize(PythonObject object) {
		if (object == null){
			serialize(false); // nonnull
		} else {
			serialize(object.getTag());
			if (primitive(object)){
				serialize(true); // nonnull
				serialize(true); // primitive
				object.serializeInnerState((PySerializer)this);
			} else {
				serialize(true); // nonnull
				serialize(false); // primitive
				if (serialized.contains(object.linkName)){
					serialize(true);
					serialize(object.linkName);
				} else {
					serialized.add(object.linkName);
					object.serializeInnerState((PySerializer)this);
				}
			}
		}
	}
	
	protected boolean primitive(PythonObject object) {
		byte tag = object.getTag();
		return tag == Tags.INT || tag == Tags.LONG || tag == Tags.DOUBLE || tag == Tags.FLOAT 
				|| tag == Tags.STRING;
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
	public void serializeJava(Object o){
		try {
			if (o instanceof PythonObject){
				serialize((PythonObject)o);
			} else {
				if (objectCache.containsKey(o)){
					serialize(objectCache.get(o));
				} else {
					serialize(oid);
					objectCache.put(o, oid++);
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}

}
