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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.utils.Utils;

/**
 * Represents objects with __contains__
 * @author Enerccio
 *
 */
public abstract class ContainerObject extends PythonObject {
	private static final long serialVersionUID = 1631363547607776261L;

	public static final String __CONTAINS__ = "__contains__";
	public static final String __LEN__ = "__len__";
	
	private static Map<String, AugumentedPythonObject> sfields = Collections.synchronizedMap(new HashMap<String, AugumentedPythonObject>());
	
	static {
		try {
			Utils.putPublic(sfields, __CONTAINS__, new JavaMethodObject(null, ContainerObject.class.getMethod("__contains__", 
					new Class<?>[]{PythonObject.class}), false));
			Utils.putPublic(sfields, __LEN__, new JavaMethodObject(null, ContainerObject.class.getMethod("__len__", 
					new Class<?>[]{}), false));
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void newObject(){
		super.newObject();
		String m;
		
		m = __CONTAINS__;
		fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
				AccessRestrictions.PUBLIC));
		m = __LEN__;
		fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
				AccessRestrictions.PUBLIC));
	}
	
	public PythonObject __contains__(PythonObject o){
		return BoolObject.fromBoolean(containsItem(o));
	}
	
	public PythonObject __len__(){
		return IntObject.valueOf(len());
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
	
	@Override
	public final boolean truthValue(){
		return len() != 0;
	}

}
