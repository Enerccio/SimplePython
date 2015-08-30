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
package me.enerccio.sp.types.callables;

import java.lang.reflect.Method;

import me.enerccio.sp.serialization.PySerializer;

/**
 * Java function wrapped into callable object.
 * 
 * @author Enerccio
 *
 */
public class JavaFunctionObject extends JavaMethodObject {
	private static final long serialVersionUID = 5136344028944670607L;
	private boolean isWrappedMethod = false;

	public JavaFunctionObject(Method m, boolean noTypeConversion) {
		super(m, noTypeConversion);
	}

	/** Usable for static methods with default signature */
	public JavaFunctionObject(Class<?> cls, String name)
			throws NoSuchMethodException, SecurityException {
		super(cls, name);
	}

	@Override
	protected String doToString() {
		return super.doToString();
	}

	public boolean isWrappedMethod() {
		return isWrappedMethod;
	}

	public void setWrappedMethod(boolean isWrappedMethod) {
		this.isWrappedMethod = isWrappedMethod;
	}
	
	@Override
	protected void serializeDirectState(PySerializer pySerializer) {
		super.serializeDirectState(pySerializer);
		pySerializer.serialize(isWrappedMethod);
	}
}
