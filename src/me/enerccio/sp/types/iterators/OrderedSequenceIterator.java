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
import java.util.Set;

import me.enerccio.sp.errors.StopIteration;
import me.enerccio.sp.interpret.AbstractPythonInterpreter;
import me.enerccio.sp.serialization.PySerializer;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.Tags;
import me.enerccio.sp.types.base.NumberObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.sequences.SequenceObject;
import me.enerccio.sp.utils.Utils;

/**
 * Sequence iterator for system classes list and tuple
 * 
 * @author Enerccio
 *
 */
public class OrderedSequenceIterator extends PythonObject implements
		InternalIterator {
	private static final long serialVersionUID = 4746975236443204424L;
	private SequenceObject sequence;
	private int cp = 0;
	private int len = 0;

	@Override
	public byte getTag() {
		return Tags.OSI;
	}

	@Override
	protected void serializeDirectState(PySerializer pySerializer) {
		pySerializer.serialize(cp);
		pySerializer.serialize(len);
		pySerializer.serialize(sequence);
	}

	public OrderedSequenceIterator(SequenceObject sequenceObject) {
		super(false);
		this.sequence = sequenceObject;
		this.len = sequence.len();
	}

	private static Map<String, JavaMethodObject> sfields = new HashMap<String, JavaMethodObject>();

	static {
		try {
			sfields.putAll(PythonObject.getSFields());
			sfields.put(SequenceObject.__ITER__, JavaMethodObject.noArgMethod(
					OrderedSequenceIterator.class, "__iter__"));
			sfields.put(GeneratorObject.NEXT, JavaMethodObject.noArgMethod(
					OrderedSequenceIterator.class, "next"));
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
	public PythonObject __iter__() {
		return this;
	}

	@Override
	public PythonObject next() {
		if (cp >= len)
			throw new StopIteration();
		PythonObject value = AbstractPythonInterpreter.interpreter.get()
				.execute(false,
						Utils.get(sequence, SequenceObject.__GETITEM__), null,
						NumberObject.valueOf(cp++));
		return value;
	}

	@Override
	public PythonObject nextInternal() {
		if (cp >= len)
			return null;
		PythonObject value = AbstractPythonInterpreter.interpreter.get()
				.execute(false,
						Utils.get(sequence, SequenceObject.__GETITEM__), null,
						NumberObject.valueOf(cp++));
		return value;
	}

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	protected String doToString() {
		return "<iterator of " + clampIter(sequence.toString()) + ">";
	}

	private String clampIter(String string) {
		if (string.length() > 50)
			return string.substring(0, 50) + "...";
		return string;
	}

}
