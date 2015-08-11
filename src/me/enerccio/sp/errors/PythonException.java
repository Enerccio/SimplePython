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
package me.enerccio.sp.errors;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.enerccio.sp.interpret.StackElement;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.sequences.ListObject;


/**
 * Represents root exception that can be raised by SimplePython
 * @author Enerccio
 *
 */
public abstract class PythonException extends RuntimeException {
	private static final long serialVersionUID = 1446541378354L;
	private static final Map<ClassObject, Class<? extends PythonException>> TYPE_TO_EXCEPTION = new HashMap<>();
	static {
		TYPE_TO_EXCEPTION.put(PythonRuntime.INDEX_ERROR, IndexError.class);
		TYPE_TO_EXCEPTION.put(PythonRuntime.VALUE_ERROR, ValueError.class);
		TYPE_TO_EXCEPTION.put(PythonRuntime.TYPE_ERROR, TypeError.class);
	}
	
	public final String message;
	public final ClassObject type;
	
	public PythonException(ClassObject type, String message, Throwable t) {
		super(type.get(ClassObject.__NAME__, null).toString() + ":" + message, t);
		this.type = type;
		this.message = message;
	}
	
	public PythonException(ClassObject type, String message) {
		super(type.get(ClassObject.__NAME__, null).toString() + ":" + message);
		this.type = type;
		this.message = message;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static PythonException translate(PythonObject e) {
		Class<? extends PythonException> jcls = null;
		String message;
		try {
			jcls = TYPE_TO_EXCEPTION.get((ClassObject) e.get(ClassObject.__CLASS__, null));
			message = e.get("__message__", null).toString();
			if (jcls == null)
				return null;
			PythonException pe = jcls.getConstructor(String.class).newInstance(message);; 
			pe.addPythonStack((List)((ListObject)e.get("stack", null)).objects);
			return pe; 
		} catch (ClassCastException | NullPointerException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ce) {
			return null;
		}
	}
	
	public void addPythonStack(List<StackElement> trace) {
		// Collections.reverse(pstack);
		List<StackTraceElement> stl = new ArrayList<StackTraceElement>(Arrays.asList(getStackTrace()));
		for (StackElement se : trace)
			stl.add(0, new StackTraceElement(se.module.getName(), se.function, se.module.getFileName(), se.line));
		setStackTrace(stl.toArray(new StackTraceElement[stl.size()]));
	}
}
