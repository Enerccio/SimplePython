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
package me.enerccio.sp.interpret;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.enerccio.sp.errors.IndexError;
import me.enerccio.sp.errors.NameError;
import me.enerccio.sp.serialization.PySerializer;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.Tags;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.callables.JavaMethodObject;

/**
 * Environment object represents environment. Environment is responsible for
 * fetching variable values
 * 
 * @author Enerccio
 *
 */
public class EnvironmentObject extends PythonObject {
	private static final long serialVersionUID = -4678903433798210010L;
	private List<InternalDict> environments = new ArrayList<InternalDict>();
	
	@Override
	public byte getTag() {
		return Tags.EO;
	}
	
	@Override
	protected void serializeDirectState(PySerializer pySerializer) {
		pySerializer.serialize(environments.size());
		for (InternalDict e : environments)
			pySerializer.serialize((PythonObject) e);
	}

	public EnvironmentObject() {
		super(false);
	}

	private static Map<String, JavaMethodObject> sfields = new HashMap<String, JavaMethodObject>();

	static {
		try {
			sfields.putAll(PythonObject.getSFields());
			sfields.put("__len__", JavaMethodObject.noArgMethod(
					EnvironmentObject.class, "numberOfEnvironments"));
			sfields.put("__getitem__", new JavaMethodObject(
					EnvironmentObject.class, "getEnv", int.class));
			sfields.put("resolve_local", new JavaMethodObject(
					EnvironmentObject.class, "resolveLocal", String.class));
			sfields.put("resolve_nonlocal", new JavaMethodObject(
					EnvironmentObject.class, "resolveNonLocal", String.class));
			sfields.put("resolve_global", new JavaMethodObject(
					EnvironmentObject.class, "resolveGlobal", String.class));
		} catch (Exception e) {
			throw new RuntimeException("Fuck", e);
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

	public int numberOfEnvironments() {
		return environments.size();
	}

	public PythonObject resolveLocal(String key) {
		PythonObject value = get(key, false, false);
		if (value == null)
			throw new NameError("key '" + key + "' has no local binding");
		return value;
	}

	public PythonObject resolveNonLocal(String key) {
		PythonObject value = get(key, false, false);
		if (value == null)
			throw new NameError("key '" + key + "' has no non-local binding");
		return value;
	}

	public PythonObject resolveGlobal(String key) {
		PythonObject value = get(key, false, false);
		if (value == null)
			throw new NameError("key '" + key + "' has no global binding");
		return value;
	}

	public PythonObject getEnv(int idx) {
		if (idx < 0 || idx >= environments.size())
			throw new IndexError("__getitem__(): index out of bounds");
		return (PythonObject) environments.get(idx);
	}

	/**
	 * Adds closure maps to the environment
	 * 
	 * @param closures
	 */
	public void add(InternalDict... closures) {
		environments.addAll(Arrays.asList(closures));
	}

	/**
	 * Adds closure maps to the environment
	 * 
	 * @param closures
	 */
	public void add(Collection<InternalDict> closures) {
		environments.addAll(closures);
	}

	/**
	 * Returns bound value to variable
	 * 
	 * @param key
	 *            name of the variable
	 * @param isGlobal
	 *            is asking for global variable or not
	 * @param skipFirst
	 *            whether to skip first closure (skip locals)
	 * @return value or null if none exists
	 */
	public PythonObject get(String key, boolean isGlobal, boolean skipFirst) {
		int it = skipFirst ? 1 : 0;
		if (isGlobal) {
			it = environments.size() > 1 ? environments.size() - 2 : 0;
		}

		PythonObject o;
		for (int i = it; i < environments.size(); i++) {
			InternalDict e = environments.get(i);
			o = e.getVariable(key);
			if (o != null)
				return o;
		}
		return null;
	}

	/**
	 * Sets the variable key to the value.
	 * 
	 * @param key
	 *            name of the variable
	 * @param value
	 *            value to set
	 * @param isGlobal
	 *            is asking for global variable or not
	 * @param skipFirst
	 *            whether to skip first closure (skip locals)
	 * @return value or null if none exists
	 * @return
	 */
	public PythonObject set(String key, PythonObject value, boolean isGlobal,
			boolean skipFirst) {

		int it = skipFirst ? 1 : 0;
		if (isGlobal) {
			it = environments.size() > 1 ? environments.size() - 2 : 0;
		}

		PythonObject o;
		for (int i = it; i < environments.size(); i++) {
			if (!isGlobal && environments.size() == 2)
				break; // ignore builtin when set to non global
			if (!isGlobal && environments.size() > 1
					&& i == environments.size() - 2)
				break; // ignore globals when setting without globals
			if (environments.size() > 1 && i == environments.size() - 1)
				break; // ignore builtins

			InternalDict e = environments.get(i);
			o = e.getVariable(key);
			if (o != null) {
				e.putVariable(key, value);
				return NoneObject.NONE;
			}
		}

		environments.get(0).putVariable(key, value);
		return NoneObject.NONE;
	}

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	protected String doToString() {
		return "<environment 0x" + Integer.toHexString(hashCode()) + ">";
	}

	/**
	 * Returns locals in this environment
	 * 
	 * @return
	 */
	public InternalDict getLocals() {
		return environments.get(0);
	}

	public void pushLocals(InternalDict locals) {
		environments.add(0, locals);
	}

	public PythonObject getBuiltin(String key) {
		return environments.get(environments.size() - 1).getVariable(key);
	}

	public List<InternalDict> toClosure() {
		return new ArrayList<InternalDict>(environments);
	}

	public void delete(String vname, boolean isGlobal) {
		if (environments.size() == 1) {
			delete(vname, false);
			return;
		} else {
			if (isGlobal) {
				if (environments.get(0).containsVariable(vname)) {
					environments.get(environments.size() - 2).remove(vname);
					return;
				}
			} else {
				if (environments.get(0).containsVariable(vname)) {
					environments.get(0).remove(vname);
					return;
				}
			}
		}
		throw new NameError("name '" + vname.toString() + "' is not defined");
	}

	public InternalDict getGlobals() {
		if (environments.size() == 1)
			return environments.get(0);
		else
			return environments.get(environments.size() - 2);
	}
}
