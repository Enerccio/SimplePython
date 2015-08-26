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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.enerccio.sp.errors.AttributeError;
import me.enerccio.sp.errors.KeyError;
import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.interpret.InternalDict;
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.interpret.KwArgs.HashMapKWArgs;
import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.ContainerObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.sequences.ListObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class StringDictObject extends ContainerObject implements InternalDict,
		Dictionary {
	private static final long serialVersionUID = 200L;
	public static final String __GETITEM__ = "__getitem__";
	public static final String __SETITEM__ = "__setitem__";
	public static final String __LEN__ = "__len__";

	public StringDictObject() {
		super(false);
	}

	private static Map<String, JavaMethodObject> sfields = new HashMap<String, JavaMethodObject>();

	static {
		try {
			sfields.putAll(ContainerObject.getSFields());
			sfields.put(__GETITEM__, new JavaMethodObject(
					StringDictObject.class, "getItem"));
			sfields.put(__SETITEM__, new JavaMethodObject(
					StringDictObject.class, "setItem"));
			sfields.put("keys", new JavaMethodObject(StringDictObject.class,
					"keys"));
			sfields.put("values", new JavaMethodObject(StringDictObject.class,
					"values"));
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	protected static Map<String, JavaMethodObject> getSFields() {
		return sfields;
	}

	@Override
	public Set<String> getGenHandleNames() {
		return sfields.keySet();
	}

	@Override
	protected Map<String, JavaMethodObject> getGenHandles() {
		return sfields;
	}

	public Map<String, PythonObject> backingMap = new HashMap<String, PythonObject>();

	@Override
	public PythonObject set(String key, PythonObject localContext,
			PythonObject value) {
		if (!fields.containsKey(key))
			throw new AttributeError("'"
					+ Utils.run("str", Utils.run("typename", this))
					+ "' object has no attribute '" + key + "'");
		throw new AttributeError("'"
				+ Utils.run("str", Utils.run("typename", this))
				+ "' object attribute '" + key + "' is read only");
	}

	@Override
	public void create(String key, AccessRestrictions restrictions,
			PythonObject localContext) {

	}

	// Internal use only
	public synchronized boolean contains(String key) {
		synchronized (backingMap) {
			return backingMap.containsKey(key);
		}
	}

	public synchronized void put(String key, PythonObject value) {
		synchronized (backingMap) {
			backingMap.put(key, value);
		}
	}

	@Override
	public synchronized PythonObject getItem(String key) {
		return backingMap.get(key);
	}

	public synchronized PythonObject getItem(TupleObject a, KwArgs kwargs) {
		if (kwargs != null)
			kwargs.checkEmpty(__GETITEM__);
		if (a.len() != 1)
			throw new TypeError("__getitem__(): requires 1 parameter");
		PythonObject key = a.getObjects()[0];
		if (!(key instanceof StringObject))
			throw new TypeError(
					"__getitem__(): internal string dict only allows string keys");
		synchronized (backingMap) {
			if (!backingMap.containsKey(((StringObject) key).value))
				throw new KeyError("Unknown key " + key);
			return backingMap.get(((StringObject) key).value);
		}
	}

	public synchronized PythonObject setItem(TupleObject a, KwArgs kwargs) {
		if (kwargs != null)
			kwargs.checkEmpty(__SETITEM__);
		if (a.len() != 2)
			throw new TypeError("__setitem__(): requires 2 parameters");
		PythonObject key = a.getObjects()[0];
		PythonObject value = a.getObjects()[1];
		if (!(key instanceof StringObject))
			throw new TypeError(
					"__setitem__(): internal string dict only allows string keys");

		synchronized (backingMap) {
			return backingMap.put(((StringObject) key).value, value);
		}
	}

	@Override
	public int len() {
		synchronized (backingMap) {
			return backingMap.size();
		}
	}

	public synchronized PythonObject doGet(String str) {
		return backingMap.get(str);
	}

	public PythonObject keys(TupleObject t, KwArgs kwargs) {
		if (kwargs != null)
			kwargs.checkEmpty("keys");
		ListObject o = new ListObject();
		synchronized (backingMap) {
			for (String k : backingMap.keySet())
				o.append(new StringObject(k));
		}
		return o;
	}

	public Set<String> keys() {
		Set<String> rv = new HashSet<>();
		synchronized (backingMap) {
			for (String k : backingMap.keySet())
				rv.add(k);
		}
		return rv;
	}

	public PythonObject values(TupleObject t, KwArgs kwargs) {
		if (kwargs != null)
			kwargs.checkEmpty("values");
		ListObject o = new ListObject();
		synchronized (backingMap) {
			for (PythonObject k : backingMap.values())
				o.append(k);
		}
		return o;
	}

	private static ThreadLocal<Set<StringDictObject>> printMap = new ThreadLocal<Set<StringDictObject>>() {

		@Override
		protected Set<StringDictObject> initialValue() {
			return new HashSet<StringDictObject>();
		}

	};

	@Override
	protected String doToString() {
		if (printMap.get().contains(this))
			return "...";
		else
			try {
				printMap.get().add(this);
				return backingMap.toString();
			} finally {
				printMap.get().remove(this);
			}
	}

	public StringDictObject cloneMap() {
		StringDictObject c = new StringDictObject();
		synchronized (backingMap) {
			c.backingMap.putAll(backingMap);
		}
		return c;
	}

	@Override
	public int getId() {
		throw new TypeError("unhashable type '" + Utils.run("typename", this)
				+ "'");
	}

	@Override
	public boolean containsItem(PythonObject o) {
		synchronized (backingMap) {
			return backingMap.containsKey(o);
		}
	}

	@Override
	public HashMap<String, PythonObject> asStringDict() {
		HashMap<String, PythonObject> strDict = new HashMap<String, PythonObject>();
		strDict.putAll(backingMap);
		return strDict;
	}

	@Override
	public synchronized void deleteKey(PythonObject key) {
		if (!backingMap.containsKey(key))
			throw new KeyError("__delkey__(): unknown key '" + key.toString()
					+ "'");
		backingMap.remove(key);
	}

	public synchronized Map<PythonObject, PythonObject> asRegularDict() {
		Map<PythonObject, PythonObject> map = new HashMap<PythonObject, PythonObject>();

		for (String s : backingMap.keySet()) {
			map.put(new StringObject(s), backingMap.get(s));
		}

		return map;
	}

	@Override
	public boolean containsVariable(String vName) {
		return backingMap.containsKey(vName);
	}

	@Override
	public PythonObject getVariable(String vName) {
		return backingMap.get(vName);
	}

	@Override
	public void putVariable(String vName, PythonObject v) {
		backingMap.put(vName, v);
	}

	@Override
	public void remove(String vname) {
		backingMap.remove(vname);
	}

	@Override
	public Set<String> keySet() {
		return keys();
	}

	@Override
	public KwArgs asKwargs() {
		HashMapKWArgs kwargs = new HashMapKWArgs();
		for (String key : backingMap.keySet())
			kwargs.put(key, backingMap.get(key));
		return kwargs;
	}
}
