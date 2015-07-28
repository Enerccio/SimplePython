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
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.BoolObject;
import me.enerccio.sp.types.base.ContainerObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.iterators.XRangeIterator;
import me.enerccio.sp.utils.Utils;

/**
 * xrange implementation
 */
public class XRangeObject extends PythonObject implements SimpleIDAccessor  {
	private static final long serialVersionUID = -543998207864616108L;
	public static final String __REVERSED__ = "__reversed__";
	public static final String __CONTAINS__ = ContainerObject.__CONTAINS__;
	public static final String __LEN__ = ContainerObject.__LEN__;
	public static final String __ITER__ = SequenceObject.__ITER__;
	public static final String __GETITEM__ = SequenceObject.__GETITEM__;
			
	private int start, end, step;

	public XRangeObject(int start, int end, int step) {
		this.start = start;
		this.end = end;
		this.step = step;
	}
	
	private static Map<String, AugumentedPythonObject> sfields = Collections.synchronizedMap(new HashMap<String, AugumentedPythonObject>());
	
	static {
		try {
			Utils.putPublic(sfields, __REVERSED__, new JavaMethodObject(null, XRangeObject.class.getMethod("__reversed__", 
					new Class<?>[]{TupleObject.class}), true));
			Utils.putPublic(sfields, __CONTAINS__, new JavaMethodObject(null, XRangeObject.class.getMethod("__contains__", 
					new Class<?>[]{PythonObject.class}), true));
			Utils.putPublic(sfields, __LEN__, new JavaMethodObject(null, XRangeObject.class.getMethod("__len__", 
					new Class<?>[]{TupleObject.class}), true));
			Utils.putPublic(sfields, __ITER__, new JavaMethodObject(null, XRangeObject.class.getMethod("__iter__", 
					new Class<?>[]{TupleObject.class}), true));
			Utils.putPublic(sfields, __GETITEM__, new JavaMethodObject(null, XRangeObject.class.getMethod("get", 
					new Class<?>[]{PythonObject.class}), true));
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
		m = __CONTAINS__;
		fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
				AccessRestrictions.PUBLIC));
		m = __LEN__;
		fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
				AccessRestrictions.PUBLIC));
		m = __REVERSED__;
		fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
				AccessRestrictions.PUBLIC));
	}
	
	@Override
	protected String doToString() {
		StringBuilder sb = new StringBuilder();
		sb.append("xrange(");
		if (start != 0) {
			sb.append(start);
			sb.append(", ");
		}
		sb.append(end);
		if (step != 1) {
			sb.append(", ");
			sb.append(step);
		}
		sb.append(")");
		return sb.toString();
	}

	public PythonObject get(PythonObject key) {
		if (key instanceof IntObject)
			return IntObject.valueOf(start + step * ((IntObject)key).intValue());
		throw Utils.throwException("TypeError", "sequence index must be integer, not '" + key.getType() + "'");
	}

	public PythonObject __iter__(TupleObject t) {
		XRangeIterator rv = new XRangeIterator(start, end, step);
		rv.newObject();
		return rv;
	}
	
	public PythonObject __reversed__(TupleObject t) {
		XRangeIterator rv = new XRangeIterator(end - 1, start - 1, - step);
		rv.newObject();
		return rv;
	}

	public PythonObject __len__(TupleObject t){
		return IntObject.valueOf(len());
	}
	
	public PythonObject __contains__(PythonObject o) {
		return BoolObject.FALSE;
	}

	@Override
	public int len() {
		return end - start / step;
	}

	@Override
	public PythonObject valueAt(int idx) {
		return get(IntObject.valueOf(idx));
	}
	
	@Override
	public boolean truthValue() {
		return len() > 0;
	}
}