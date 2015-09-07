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
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.interpret.FrameObject;
import me.enerccio.sp.serialization.PySerializer;
import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.Tags;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.callables.UserMethodObject;
import me.enerccio.sp.types.sequences.SequenceObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class GeneratorObject extends PythonObject {
	private static final long serialVersionUID = -3004146816129145535L;

	private static Map<String, JavaMethodObject> sfields = new HashMap<String, JavaMethodObject>();

	public static final String __ITER__ = SequenceObject.__ITER__;
	public static final String NEXT = "next";
	public static final String SEND = "send";
	public static final String THROW = "throw";
	public static final String CLOSE = "close";
	public static final String __DEL__ = "__del__";

	static {
		try {
			sfields.putAll(PythonObject.getSFields());
			sfields.put(__ITER__, JavaMethodObject.noArgMethod(
					GeneratorObject.class, "__iter__"));
			sfields.put(NEXT,
					JavaMethodObject.noArgMethod(GeneratorObject.class, "next"));
			sfields.put(SEND, new JavaMethodObject(GeneratorObject.class,
					"send", PythonObject.class));
			sfields.put(THROW, new JavaMethodObject(GeneratorObject.class,
					"throwException", ClassObject.class, PythonObject.class));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public byte getTag() {
		return Tags.GEN;
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

	public GeneratorObject(String name, List<FrameObject> o) {
		super(false);
		this.name = name;
		this.storedFrames = o;
	}

	private String name;
	public List<FrameObject> storedFrames;

	@Override
	protected void serializeDirectState(PySerializer pySerializer) {
		pySerializer.serialize(name);
		pySerializer.serialize(storedFrames.size());
		for (FrameObject o : storedFrames)
			pySerializer.serialize(o);
	}

	@Override
	public void newObject() {
		super.newObject();

		PythonObject fnc = Utils.getGlobal("close_generator");

		PythonObject value = new UserMethodObject();
		Utils.putPublic(value, UserMethodObject.SELF, this);
		Utils.putPublic(value, UserMethodObject.FUNC, fnc);
		Utils.putPublic(value, UserMethodObject.ACCESSOR, NoneObject.NONE);

		fields.put(CLOSE, new AugumentedPythonObject(value,
				AccessRestrictions.PUBLIC));
		fields.put(__DEL__, new AugumentedPythonObject(value,
				AccessRestrictions.PUBLIC));
	}

	public PythonObject __iter__() {
		return this;
	}

	private volatile boolean nextCalled = false;

	public synchronized PythonObject next() {
		nextCalled = true;
		return send(NoneObject.NONE);
	}

	public synchronized PythonObject send(PythonObject v) {
		if (!nextCalled && v != NoneObject.NONE)
			throw new TypeError("send(): send called before first next called");
		for (FrameObject o : this.storedFrames)
			PythonInterpreter.interpreter.get().currentFrame.add(o);
		this.storedFrames.get(this.storedFrames.size() - 1).sendValue = v;
		return NoneObject.NONE;
	}

	public synchronized PythonObject throwException(ClassObject cls,
			PythonObject v) {
		this.storedFrames.get(this.storedFrames.size() - 1).exception = cls
				.call(new TupleObject(true, v), null);
		return send(NoneObject.NONE);
	}

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	protected String doToString() {
		return "<generator object '" + name + "' at 0x"
				+ Integer.toHexString(hashCode()) + ">";
	}

}
