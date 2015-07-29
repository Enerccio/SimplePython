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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.Arithmetics;
import me.enerccio.sp.types.AugumentedPythonObject;
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
	
	private static Map<String, AugumentedPythonObject> sfields = Collections.synchronizedMap(new HashMap<String, AugumentedPythonObject>());
	
	static {
		try {
			Utils.putPublic(sfields, __ITER__, new JavaMethodObject(null, SequenceObject.class.getMethod("__iter__", 
					new Class<?>[]{TupleObject.class}), true));
			Utils.putPublic(sfields, __GETITEM__, new JavaMethodObject(null, SequenceObject.class.getMethod("get", 
					new Class<?>[]{PythonObject.class}), false));
			Utils.putPublic(sfields, __ADD__, new JavaMethodObject(null, SequenceObject.class.getMethod("add", 
					new Class<?>[]{PythonObject.class}), false));
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void newObject() {
		super.newObject();
		
		String m;
		
		m = __ITER__;
		fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
				AccessRestrictions.PUBLIC));
		m = __GETITEM__;
		fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
				AccessRestrictions.PUBLIC));
		m = __ADD__;
		fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
				AccessRestrictions.PUBLIC));
	}
	
	public PythonObject add(PythonObject other){
		return Arithmetics.doOperator(this, other, __ADD__);
	}
	
	public abstract PythonObject get(PythonObject key);
	
	public PythonObject __iter__(TupleObject args){
		if (args.len() > 0)
			throw Utils.throwException("TypeError", "__iter__(): method requires no arguments");
		return createIterator();
	}
	
	public abstract PythonObject createIterator(); 

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
