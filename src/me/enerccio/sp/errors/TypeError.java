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

import me.enerccio.sp.runtime.PythonRuntime;

public class TypeError extends BasePythonError {
	private static final long serialVersionUID = 9845123L;

	public TypeError(String message, Throwable cause) {
		super(PythonRuntime.TYPE_ERROR, message, cause);
	}

	public TypeError(String message) {
		super(PythonRuntime.TYPE_ERROR, message, null);
	}

	public TypeError(String message, Exception e) {
		super(PythonRuntime.TYPE_ERROR, message, e);
	}
}
