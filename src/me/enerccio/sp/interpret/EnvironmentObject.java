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
import java.util.List;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.mappings.DictObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.utils.Utils;

/**
 * Environment object represents environment. Environment is responsible for fetching variable values
 * @author Enerccio
 *
 */
public class EnvironmentObject extends PythonObject {
	private static final long serialVersionUID = -4678903433798210010L;
	private List<DictObject> environments = new ArrayList<DictObject>();
	
	/**
	 * Adds closure maps to the environment
	 * @param closures
	 */
	public void add(DictObject... closures){
		environments.addAll(Arrays.asList(closures));
	}
	
	/**
	 * Adds closure maps to the environment
	 * @param closures
	 */
	public void add(Collection<DictObject> closures){
		environments.addAll(closures);
	}
	
	/**
	 * Returns bound value to variable
	 * @param key name of the variable
	 * @param isGlobal is asking for global variable or not
	 * @param skipFirst whether to skip first closure (skip locals)
	 * @return value or null if none exists
	 */
	public PythonObject get(StringObject key, boolean isGlobal, boolean skipFirst){
		int it = skipFirst ? 1 : 0;
		if (isGlobal){
			it = environments.size() > 1 ? environments.size()-2 : 0;
		}
		
		PythonObject o;
		for (int i=it; i<environments.size(); i++){
			DictObject e = environments.get(i);
			o = e.doGet(key);
			if (o != null)
				return o;
		}
		return null;
	}
	
	/**
	 * Sets the variable key to the value. 
	 * @param key name of the variable
	 * @param value value to set
	 * @param isGlobal is asking for global variable or not
	 * @param skipFirst whether to skip first closure (skip locals)
	 * @return value or null if none exists
	 * @return
	 */
	public PythonObject set(StringObject key, PythonObject value, boolean isGlobal, boolean skipFirst){
		
		int it = skipFirst ? 1 : 0;
		if (isGlobal){
			it = environments.size() > 1 ? environments.size()-2 : 0;
		}
		
		PythonObject o;
		for (int i=it; i<environments.size(); i++){
			if (environments.size() > 1 && i == environments.size()-1)
				break; // ignore builtins
			
			DictObject e = environments.get(i);
			o = e.doGet(key);
			if (o != null){
				return e.backingMap.put(key, value);
			}
		}
		
		return environments.get(0).backingMap.put(key, value);
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
	 * @return
	 */
	public DictObject getLocals() {
		return environments.get(0);
	}
	
	public void pushLocals(DictObject locals){
		environments.add(0, locals);
	}
	
	public PythonObject getBuiltin(String key){
		return environments.get(environments.size()-1).doGet(key);
	}

	public List<DictObject> toClosure() {
		return new ArrayList<DictObject>(environments);
	}

	public void delete(StringObject vname, boolean isGlobal) {
		if (environments.size() == 1){
			delete(vname, false);
			return;
		} else {
			if (isGlobal) {
				if (environments.get(0).backingMap.containsKey(vname)){
					environments.get(environments.size()-2).backingMap.remove(vname);
					return;
				}
			} else {
				if (environments.get(0).backingMap.containsKey(vname)){
					environments.get(0).backingMap.remove(vname);
					return;
				} 
			}
		}
		throw Utils.throwException("NameError", "name '" + vname.toString() + "' is not defined");
	}
}
