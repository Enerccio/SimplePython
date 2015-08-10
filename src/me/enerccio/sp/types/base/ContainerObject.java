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
package me.enerccio.sp.types.base;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.JavaMethodObject;

/**
 * Represents objects with __contains__
 * @author Enerccio
 *
 */
public abstract class ContainerObject extends PythonObject {
	private static final long serialVersionUID = 1631363547607776261L;

	public static final String __CONTAINS__ = "__contains__";
	public static final String __DELKEY__ = "__delkey__";
	public static final String __LEN__ = "__len__";
	
	private static Map<String, JavaMethodObject> sfields = new HashMap<String, JavaMethodObject>();
	
	static {
		try {
			sfields.putAll(PythonObject.getSFields());
			sfields.put(__CONTAINS__, 	new JavaMethodObject(ContainerObject.class, __CONTAINS__, PythonObject.class));
			sfields.put(__DELKEY__, 	new JavaMethodObject(ContainerObject.class, __DELKEY__, PythonObject.class)); 
			sfields.put( __LEN__, 		JavaMethodObject.noArgMethod(ContainerObject.class, __LEN__));
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	protected static Map<String, JavaMethodObject> getSFields(){ return sfields; }
	
	@Override
	public void newObject(){
		super.newObject();
	}

	@Override
	public Set<String> getGenHandleNames() {
		return sfields.keySet();
	}

	@Override
	protected Map<String, JavaMethodObject> getGenHandles() {
		return sfields;
	}
	
	public PythonObject __contains__(PythonObject o){
		return BoolObject.fromBoolean(containsItem(o));
	}
	
	public PythonObject __len__(){
		return IntObject.valueOf(len());
	}
	
	public PythonObject __delkey__(PythonObject key){
		deleteKey(key);
		return NoneObject.NONE;
	}

	/**
	 * returns true if object o is in this container
	 * @param o
	 * @return
	 */
	public abstract boolean containsItem(PythonObject o);

	/**
	 * returns number of elements in this container
	 * @return
	 */
	public abstract int len();
	
	/**
	 * Removes key from the container or throws attribute error
	 * @param key to remove
	 */
	public abstract void deleteKey(PythonObject key);
	
	@Override
	public final boolean truthValue(){
		return len() != 0;
	}

}
