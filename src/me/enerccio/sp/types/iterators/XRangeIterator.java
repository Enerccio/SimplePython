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
package me.enerccio.sp.types.iterators;

import java.util.HashMap;
import java.util.Map;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.sequences.SequenceObject;
import me.enerccio.sp.utils.Utils;

/**
 * xrange implementation
 */
public class XRangeIterator extends PythonObject implements InternalIterator {
	private static final long serialVersionUID = -543998207864616108L;
	public static final String __ITER__ = SequenceObject.__ITER__;
	public static final String NEXT = GeneratorObject.NEXT;
			
	private int i, end, step;

	public XRangeIterator(int start, int end, int step) {
		this.i = start;
		this.end = end;
		this.step = step;
	}
	
	private static Map<String, JavaMethodObject> sfields = new HashMap<String, JavaMethodObject>();
	
	static {
		try {
			sfields.put(__ITER__,	JavaMethodObject.noArgMethod(XRangeIterator.class, "__iter__"));
			sfields.put(NEXT, 		JavaMethodObject.noArgMethod(XRangeIterator.class, "next"));
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void newObject() {
		super.newObject();
		bindMethods(sfields);
	}
	
	@Override
	protected String doToString() {
		return "<rangeiterator object at " + this.getId() + ">";
	}

	@Override
	public PythonObject __iter__() {
		return this;
	}
	
	@Override
	public PythonObject nextInternal() {
		if (step > 0) {
			// Goes up
			if (i >= end)
				return null;
		} else {
			// Goes down
			if (i <= end)
				return null;
		}
		IntObject rv = IntObject.valueOf(i);
		i += step;
		return rv;
	}
	
	@Override
	public PythonObject next() {
		PythonObject rv = nextInternal();
		if (rv == null)
			throw Utils.throwException("StopIteration");
		return rv;
	}

	@Override
	public boolean truthValue() {
		return true;
	}

}
