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

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import me.enerccio.sp.compiler.Bytecode;
import me.enerccio.sp.serialization.PySerializer;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.Tags;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.iterators.GeneratorObject;
import me.enerccio.sp.types.sequences.ListObject;

/**
 * FrameObject represents execution frame. Holds flags related to the execution
 * and also pc + bytecode
 * 
 * @author Enerccio
 *
 */
public class FrameObject extends PythonObject {
	private static final long serialVersionUID = 3202634156179178037L;

	public FrameObject() {
		super(false);
	}

	@Override
	public byte getTag() {
		return Tags.FO;
	}

	private static Map<String, JavaMethodObject> sfields = new HashMap<String, JavaMethodObject>();

	static {
		try {
			sfields.putAll(PythonObject.getSFields());
			sfields.put("current_pos", JavaMethodObject.noArgMethod(
					FrameObject.class, "currentPos"));
			sfields.put("current_stack", JavaMethodObject.noArgMethod(
					FrameObject.class, "currentStack"));
			sfields.put("get_parent", JavaMethodObject.noArgMethod(
					FrameObject.class, "getParent"));
			sfields.put("is_subframe", JavaMethodObject.noArgMethod(
					FrameObject.class, "isSubframe"));
			sfields.put("get_bound_code", JavaMethodObject.noArgMethod(
					FrameObject.class, "getCompiled"));
			sfields.put("get_bound_generator", JavaMethodObject.noArgMethod(
					FrameObject.class, "getGenerator"));
			sfields.put("get_local_context", JavaMethodObject.noArgMethod(
					FrameObject.class, "getLocalContext"));
			sfields.put("get_environment", JavaMethodObject.noArgMethod(
					FrameObject.class, "getEnvironment"));
		} catch (Exception e) {
			throw new RuntimeException("Fuck", e);
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

	public int currentPos() {
		return pc;
	}

	public PythonObject currentStack() {
		ListObject lo = new ListObject();
		for (PythonObject o : stack)
			lo.objects.add(o);
		return lo;
	}

	public FrameObject getParent() {
		return parentFrame;
	}

	public boolean isSubframe() {
		return parentFrame != null;
	}

	public CompiledBlockObject getCompiled() {
		return compiled;
	}

	public GeneratorObject getGenerator() {
		return ownedGenerator;
	}

	public PythonObject getLocalContext() {
		return localContext;
	}

	public EnvironmentObject getEnvironment() {
		return environment;
	}

	/**
	 * Parent frame is null if this is normal frame, or reference to parent
	 * frame if this is a subframe
	 */
	public FrameObject parentFrame;
	/** whether previous frame ended with return */
	public boolean returnHappened;
	/** whether this frame pushed local context or not */

	/** whether this frame wants to accept some value as return */
	public boolean accepts_return;

	/** If python exception has happened, it will be stored here */
	public PythonObject exception;
	/** Bytecode of this frame */
	public CompiledBlockObject compiled;
	/** Bytebuffer of the data */
	public ByteBuffer dataStream; // -- not serialized, recreate
	/** program counter */
	public int pc;
	/** previous pc */
	public int prevPc;
	/** python stack of this frame */
	public Stack<PythonObject> stack = new Stack<PythonObject>();
	public GeneratorObject ownedGenerator;
	public PythonObject localContext;
	public EnvironmentObject environment;

	public boolean isSignal;
	public PythonObject storedReturnee;
	public InternalDict storedArgs;
	public List<InternalDict> storedClosure;

	@Override
	protected void serializeDirectState(PySerializer pySerializer) {
		pySerializer.serialize(parentFrame);
		pySerializer.serialize(returnHappened);
		pySerializer.serialize(accepts_return);
		pySerializer.serialize(exception);
		pySerializer.serialize(compiled);
		pySerializer.serialize(pc);
		pySerializer.serialize(prevPc);

		pySerializer.serialize(stack.size());
		for (PythonObject o : stack)
			pySerializer.serialize(o);
		pySerializer.serialize(ownedGenerator);
		pySerializer.serialize(localContext);
		pySerializer.serialize(environment);
		pySerializer.serialize(isSignal);
		pySerializer.serialize(storedReturnee);
		pySerializer.serialize((PythonObject) storedArgs);
		if (storedClosure == null) {
			pySerializer.serialize(false);
		} else {
			pySerializer.serialize(true);
			pySerializer.serialize(storedClosure.size());
			for (InternalDict id : storedClosure)
				pySerializer.serialize((PythonObject) id);
		}
	}

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	protected String doToString() {
		return "<frame object 0x" + Integer.toHexString(hashCode()) + ">";
	}

	public KwArgs.HashMapKWArgs kwargs = null;

	public boolean yielding = false;
	public PythonObject sendValue;

	public Bytecode nextOpcode() {
		++pc;
		return Bytecode.fromNumber(((short) (dataStream.get() & 0xff)));
	}

	public int nextInt() {
		pc += 4;
		return dataStream.getInt();
	}

	public FrameObject cloneFrame() {
		FrameObject f = new FrameObject();
		f.pc = pc;
		f.compiled = compiled;
		f.accepts_return = accepts_return;
		f.dataStream = f.compiled.getBytedataAsNativeBuffer();
		f.prevPc = prevPc;
		f.environment = environment;
		f.exception = exception;
		f.kwargs = kwargs;
		f.localContext = localContext;
		f.ownedGenerator = ownedGenerator;
		f.returnHappened = returnHappened;
		f.yielding = yielding;
		f.sendValue = sendValue;
		f.stack = stack;

		return f;
	}
}
