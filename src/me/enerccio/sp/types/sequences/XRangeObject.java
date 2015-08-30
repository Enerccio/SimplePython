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
import java.util.Set;

import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.serialization.PySerializer;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.Tags;
import me.enerccio.sp.types.base.BoolObject;
import me.enerccio.sp.types.base.ContainerObject;
import me.enerccio.sp.types.base.NumberObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.iterators.InternallyIterable;
import me.enerccio.sp.types.iterators.XRangeIterator;

/**
 * xrange implementation
 */
public class XRangeObject extends PythonObject implements SimpleIDAccessor,
		InternallyIterable {
	private static final long serialVersionUID = -543998207864616108L;
	public static final String __REVERSED__ = "__reversed__";
	public static final String __CONTAINS__ = ContainerObject.__CONTAINS__;
	public static final String __LEN__ = ContainerObject.__LEN__;
	public static final String __ITER__ = SequenceObject.__ITER__;
	public static final String __GETITEM__ = SequenceObject.__GETITEM__;

	private int start, end, step;

	public XRangeObject(boolean internalUse, int start, int end, int step) {
		super(internalUse);
		this.start = start;
		this.end = end;
		this.step = step;
	}
	
	@Override
	public byte getTag() {
		return Tags.XRANGE;
	}
	
	@Override
	protected void serializeDirectState(PySerializer pySerializer) {
		pySerializer.serialize(start);
		pySerializer.serialize(end);
		pySerializer.serialize(step);
	}

	private static Map<String, JavaMethodObject> sfields = Collections
			.synchronizedMap(new HashMap<String, JavaMethodObject>());

	static {
		try {
			sfields.putAll(PythonObject.getSFields());
			sfields.put(__REVERSED__, new JavaMethodObject(XRangeObject.class,
					"__reversed__"));
			sfields.put(__CONTAINS__, new JavaMethodObject(XRangeObject.class,
					"__contains__", PythonObject.class));
			sfields.put(__LEN__, new JavaMethodObject(XRangeObject.class,
					"__len__"));
			sfields.put(__ITER__, JavaMethodObject.noArgMethod(
					XRangeObject.class, "__iter__"));
			sfields.put(__GETITEM__, new JavaMethodObject(XRangeObject.class,
					"valueAt", int.class));
		} catch (Exception e) {
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

	@Override
	public void newObject() {
		super.newObject();
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
		if (NumberObject.isInteger(key))
			return NumberObject.valueOf(start + step
					* ((NumberObject) key).intValue());
		throw new TypeError("sequence index must be integer, not '"
				+ key.getType() + "'");
	}

	@Override
	public PythonObject __iter__() {
		XRangeIterator rv = new XRangeIterator(start, end, step);
		return rv;
	}

	public PythonObject __reversed__(TupleObject t, KwArgs kw) {
		t.notExpectingArgs(kw);
		XRangeIterator rv = new XRangeIterator(end - 1, start - 1, -step);
		return rv;
	}

	public PythonObject __len__(TupleObject t, KwArgs kw) {
		t.notExpectingArgs(kw);
		return NumberObject.valueOf(len());
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
		return get(NumberObject.valueOf(idx));
	}

	@Override
	public boolean truthValue() {
		return len() > 0;
	}
}