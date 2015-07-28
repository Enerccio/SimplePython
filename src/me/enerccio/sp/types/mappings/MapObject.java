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
package me.enerccio.sp.types.mappings;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.ContainerObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.sequences.ListObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

/**
 * Implementation of the python dict
 * @author Enerccio
 *
 */
public class MapObject extends ContainerObject {
	private static final long serialVersionUID = 20L;
	public static final String __GETITEM__ = "__getitem__";
	public static final String __SETITEM__ = "__setitem__";
	public static final String __LEN__ = "__len__";
	
	public MapObject(){
		newObject();
	}
	
	private static Map<String, AugumentedPythonObject> sfields = Collections.synchronizedMap(new HashMap<String, AugumentedPythonObject>());
	
	static {
		try {
			Utils.putPublic(sfields, __GETITEM__, new JavaMethodObject(null, MapObject.class.getMethod("getItem", 
					new Class<?>[]{TupleObject.class}), true));
			Utils.putPublic(sfields, __SETITEM__, new JavaMethodObject(null, MapObject.class.getMethod("setItem", 
					new Class<?>[]{TupleObject.class}), true));
			Utils.putPublic(sfields, __LEN__, new JavaMethodObject(null, MapObject.class.getMethod("len", 
					new Class<?>[]{TupleObject.class}), true));
			Utils.putPublic(sfields, "keys", new JavaMethodObject(null, MapObject.class.getMethod("keys", 
					new Class<?>[]{TupleObject.class}), true));
			Utils.putPublic(sfields, "values", new JavaMethodObject(null, MapObject.class.getMethod("values", 
					new Class<?>[]{TupleObject.class}), true));
		} catch (NoSuchMethodException e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void newObject() {
		super.newObject();
		
		String m;
		
		m = __GETITEM__;
		fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
				AccessRestrictions.PUBLIC));
		m = __SETITEM__;
		fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
				AccessRestrictions.PUBLIC));
		m = __LEN__;
		fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
				AccessRestrictions.PUBLIC));
		m = "keys";
		fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
				AccessRestrictions.PUBLIC));
		m = "values";
		fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
				AccessRestrictions.PUBLIC));
		
	};
	
	public HashHashMap<PythonObject> backingMap = new HashHashMap<PythonObject>();
	
	public IntObject size(){
		synchronized (backingMap){
			return IntObject.valueOf(backingMap.size());
		}
	}

	@Override
	public PythonObject set(String key, PythonObject localContext,
			PythonObject value) {
		if (!fields.containsKey(key))
			throw Utils.throwException("AttributeError", "'" + 
					Utils.run("str", Utils.run("type", this)) + "' object has no attribute '" + key + "'");
		throw Utils.throwException("AttributeError", "'" + 
				Utils.run("str", Utils.run("type", this)) + "' object attribute '" + key + "' is read only");
	}

	@Override
	public void create(String key, AccessRestrictions restrictions, PythonObject localContext) {
		
	}

	// Internal use only
	public boolean contains(String key) {
		synchronized (backingMap){
			return backingMap.containsKey(new StringObject(key));
		}
	}

	public void put(String key, PythonObject value) {
		synchronized (backingMap){
			backingMap.put(new StringObject(key), value);
		}
	}
	
	public PythonObject getItem(TupleObject a){
		if (a.len() != 1)
			throw Utils.throwException("TypeError", "__getitem__(): requires 1 parameter");
		PythonObject key = a.getObjects()[0];
		synchronized (backingMap){
			if (!backingMap.containsKey(key))
				throw Utils.throwException("KeyError", "Unknown key " + key);
			return backingMap.get(key);
		}
	}
	
	public PythonObject setItem(TupleObject a){
		if (a.len() != 2)
			throw Utils.throwException("TypeError", "__setitem__(): requires 2 parameters");
		PythonObject key = a.getObjects()[0];
		PythonObject value = a.getObjects()[1];
		synchronized (backingMap){
			return backingMap.put(key, value);
		}
	}
	
	public PythonObject len(TupleObject a){
		if (a.len() != 0)
			throw Utils.throwException("TypeError", "__len__(): requires zero parameters");
		return size();
	}

	public PythonObject doGet(String str) {
		return doGet(new StringObject(str));
	}

	public PythonObject doGet(PythonObject key) {
		return backingMap.get(key);
	}
	
	public PythonObject keys(TupleObject t) {
		ListObject o = new ListObject();
		o.newObject();
		synchronized (backingMap) {
			for (PythonProxy k : backingMap.keySet())
				o.append(k.o);
		}
		return o;
	}
	
	public PythonObject values(TupleObject t) {
		ListObject o = new ListObject();
		o.newObject();
		synchronized (backingMap) {
			for (PythonObject k : backingMap.values())
				o.append(k);
		}
		return o;
	}
	
	private static ThreadLocal<Set<MapObject>> printMap = new ThreadLocal<Set<MapObject>>(){

		@Override
		protected Set<MapObject> initialValue() {
			return new HashSet<MapObject>();
		}
		
	};
	@Override
	protected String doToString() {
		if (printMap.get().contains(this))
			return "...";
		else try {
			printMap.get().add(this);
			return backingMap.toString();
		} finally {
			printMap.get().remove(this);
		}
	}

	public MapObject cloneMap() {
		MapObject c = new MapObject();
		synchronized (backingMap){
			c.backingMap.putAll(backingMap);
		}
		return c;
	}
	
	@Override
	public int getId(){
		throw Utils.throwException("TypeError", "unhashable type '" + Utils.run("type", this) + "'");
	}

	@Override
	protected boolean containsItem(PythonObject o) {
		synchronized (backingMap){
			return backingMap.containsKey(o);
		}
	}

	@Override
	protected int elementCount() {
		return backingMap.size();
	}
}
