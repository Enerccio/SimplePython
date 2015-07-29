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
package me.enerccio.sp.types.base;

import me.enerccio.sp.types.PythonObject;

/**
 * Represents boolean. Cannot be instantiated. 
 * @author Enerccio
 *
 */
public class BoolObject extends NumberObject {
	private static final long serialVersionUID = 7L;
	
	/** Python True */
	public static final BoolObject TRUE = new BoolObject(true);
	/** Python False */
	public static final BoolObject FALSE = new BoolObject(false);
	
	private final boolean value;
	
	private BoolObject(boolean v){
		this.value = v;
		newObject();
	}
	
	@Override
	protected void registerObject(){
		
	}
	
	@Override 
	public Long getJavaInt() {
		return value ? 1L : 0L;
	}
	
	@Override
	public double getJavaFloat() {
		return value ? 1.0 : 0.0;
	}
	
	@Override
	public boolean truthValue() {
		return value;
	}

	public static PythonObject fromBoolean(Boolean ret) {
		return ret ? TRUE : FALSE;
	}

	@Override
	protected PythonObject getIntValue() {
		return value ? IntObject.valueOf(1) : IntObject.valueOf(0);
	}

	@Override
	protected String doToString() {
		return value ? "True" : "False";
	}

}
