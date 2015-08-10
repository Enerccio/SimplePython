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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.pointer.PointerObject;
import me.enerccio.sp.types.sequences.ListObject;

/**
 * Represents exception raised in SimplePython or java code that wants to raise SimplePython's exception
 * @author Enerccio
 *
 */
public class PythonExecutionException extends RuntimeException {
	private static final long serialVersionUID = -1679058226367596212L;
	private PythonObject exception;

	public PythonExecutionException(PythonObject o){
		super(getMessage(o), o.fields.containsKey("__exception__") ? (Throwable)((PointerObject)o.fields.get("__exception__").object).getObject() : null);
		this.setException(o);
		init();
	}
	
	public PythonExecutionException(PythonObject o, Throwable cause){
		super(getMessage(o), cause);
		this.setException(o);
		o.fields.put("__exception__", new AugumentedPythonObject(new PointerObject(cause), AccessRestrictions.PUBLIC));
		init();
	}
	
	private void init() {
		StackTraceElement[] el = getStackTrace();
		
		try {
			List<StackTraceElement> stl = new ArrayList<StackTraceElement>(Arrays.asList(el));
			PythonObject stack = exception.get("stack", null);
			if (stack != null) {
				List<PythonObject> pstack = new ArrayList<PythonObject>(((ListObject)stack).objects);
				Collections.reverse(pstack);
				for (PythonObject o : pstack) {
					if (!(o instanceof StackElement))
						// Shouldn't actually happen
						continue;
					StackElement se = (StackElement)o;
					stl.add(0, new StackTraceElement(se.module.getName(), se.function, se.module.getFileName(), se.line));
				}
			}
			setStackTrace(stl.toArray(new StackTraceElement[stl.size()]));
		} catch (Exception e){
			setStackTrace(el);
		}
	}

	public static String getMessage(PythonObject o) {
		if (o.fields.containsKey("__message__") && o.fields.containsKey("__class__"))
			return o.fields.get("__class__").object.fields.get(ClassObject.__NAME__).object.toString() + ": " + o.fields.get("__message__").object.toString();
		if (o.fields.containsKey("__message__"))
			return o.fields.get("__message__").object.toString();
		return o.toString();
	}

	public PythonObject getException() {
		return exception;
	}

	public void setException(PythonObject exception) {
		this.exception = exception; // exception.fields.get("stack").object.toString().replace(">,", ">,\n")
	}	
}
