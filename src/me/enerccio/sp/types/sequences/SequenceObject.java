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
package me.enerccio.sp.types.sequences;

import java.util.HashMap;
import java.util.Map;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.Arithmetics;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.ContainerObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.base.SliceObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.utils.Utils;

/**
 * Represents objects with __iter__, __getitem__ and __add__
 * @author Enerccio
 *
 */
public abstract class SequenceObject extends ContainerObject {
	private static final long serialVersionUID = 10L;
	
	public static final String __ITER__ = "__iter__";
	public static final String __GETITEM__ = "__getitem__";
	public static final String __ADD__ = "__add__";
	
	private static Map<String, JavaMethodObject> sfields = new HashMap<String, JavaMethodObject>();
	
	static {
		try {
			sfields.put(__ITER__, 		JavaMethodObject.noArgMethod(SequenceObject.class, "__iter__"));
			sfields.put(__GETITEM__,	new JavaMethodObject(SequenceObject.class, "get", PythonObject.class));
			sfields.put(__ADD__,		new JavaMethodObject(SequenceObject.class, "add", PythonObject.class));
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void newObject() {
		super.newObject();
		bindMethods(sfields);
	}
	
	public PythonObject add(PythonObject other){
		return Arithmetics.doOperator(this, other, __ADD__);
	}
	
	public abstract PythonObject get(PythonObject key);
	
	public abstract PythonObject __iter__(); 

	@Override
	protected String doToString() {
		return null;
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
	
	/**
	 * Converts slice object into int[4] object
	 * @param size
	 * @param key
	 * @return
	 */
	protected int[] getSliceData(int size, PythonObject key){
		PythonObject sa = key.fields.get(SliceObject.START_ACCESSOR).object;
		PythonObject so = key.fields.get(SliceObject.STOP_ACCESSOR).object;
		PythonObject st = key.fields.get(SliceObject.STEP_ACCESSOR).object;
		
		boolean saex = sa != NoneObject.NONE;
		boolean soex = so != NoneObject.NONE;
		boolean stex = st != NoneObject.NONE;
		int sav = 0;
		int sov = size;
		int stv = 1;
		if (saex)
			sav = (int) ((IntObject)sa).intValue();
		if (soex)
			sov = (int) ((IntObject)so).intValue();
		if (stex)
			stv = (int) ((IntObject)st).intValue();
		
		boolean reverse = false;
		
		if (sav < 0)
			sav = Math.max(0, size-(-(sav+1)));
		if (stv < 0){
			reverse = true;
			stv = Math.abs(stv);
		}
		if (stv == 0)
			throw Utils.throwException("ValueError", "slice step cannot be zero");
		if (sov < 0)
			sov = Math.max(0, size-(-(sov+1)));
		
		sav = Math.max(sav, 0);
		sov = Math.min(size, sov);
		
		return new int[]{sav, sov, stv, reverse ? 1 : 0};
	}
}
