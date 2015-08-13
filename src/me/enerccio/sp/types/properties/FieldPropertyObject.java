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
package me.enerccio.sp.types.properties;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.utils.CastFailedException;
import me.enerccio.sp.utils.Coerce;

public class FieldPropertyObject extends PythonObject implements PropertyObject {
	private static final long serialVersionUID = 7523482748314799745L;
	
	private Field property;
	private boolean readOnly;
	private Object properter;
	
	public FieldPropertyObject(Class<?> clazz, String property, boolean readOnly) throws NoSuchFieldException, SecurityException{
		this(null, clazz, property, readOnly);
	}
	
	
	public FieldPropertyObject(Object object, Class<?> clazz, String property,
			boolean readOnly) throws NoSuchFieldException, SecurityException {
		this(object, clazz.getField(property), readOnly);
	}


	public FieldPropertyObject(Object object, Field field, boolean readOnly) {
		super(false);
		readOnly = readOnly ? true : !Modifier.isFinal(field.getModifiers());
		this.readOnly = readOnly;
		this.property = field;
		this.properter = object;
	}

	@Override
	public void set(PythonObject value){
		if (readOnly)
			throw new TypeError("field '" + property.getName() + "' is read-only");
		try {
			property.set(properter, Coerce.toJava(value, property.getType()));
		} catch (IllegalArgumentException | IllegalAccessException
				| CastFailedException e) {
			throw new TypeError("failed to access property '" + property.getName() + "'", e);
		}
	}
	
	@Override
	public PythonObject get(){
		try {
			return Coerce.toPython(property.get(properter), property.getType());
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new TypeError("failed to access property '" + property.getName() + "'", e);
		}
	}

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	protected String doToString() {
		return "<" + (readOnly ? "read-only " : " " ) + "property '" + property.getName() + "' at 0x" + Integer.toHexString(hashCode()) + ">";
	}

	@Override
	public Set<String> getGenHandleNames() {
		return PythonObject.sfields.keySet();
	}

	@Override
	protected Map<String, JavaMethodObject> getGenHandles() {
		return PythonObject.sfields;
	}
}
