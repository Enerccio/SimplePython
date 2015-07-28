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

import me.enerccio.sp.types.PythonObject;

/**
 * Represents exception raised in SimplePython or java code that wants to raise SimplePython's exception
 * @author Enerccio
 *
 */
public class PythonExecutionException extends RuntimeException {
	private static final long serialVersionUID = -1679058226367596212L;
	private PythonObject exception;

	public PythonExecutionException(PythonObject o){
		super(getMessage(o));
		this.setException(o);
	}
	
	public static String getMessage(PythonObject o) {
		if (o.fields.containsKey("__msg__") && o.fields.containsKey("__class__"))
			return o.fields.get("__class__").object.fields.get("__name__").object.toString() + ": " + o.fields.get("__msg__").object.toString();
		if (o.fields.containsKey("__msg__"))
			return o.fields.get("__msg__").object.toString();
		return o.toString();
	}

	public PythonObject getException() {
		return exception;
	}

	public void setException(PythonObject exception) {
		this.exception = exception;
	}
	
	
}
