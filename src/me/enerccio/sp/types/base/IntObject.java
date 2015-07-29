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
 * fixnum represented by big integer
 * @author Enerccio
 *
 */
public class IntObject extends NumberObject {
	private static final long serialVersionUID = 6L;
	
	public IntObject(){
		newObject();
	}
	
	private static IntObject[] baseMap = new IntObject[255];
	static {
		for (int i=0; i<255; i++)
			baseMap[i] = new IntObject(i-127);
	}
	
	public static IntObject valueOf(int v){
		if (v+127 < 255 &&  v+127 > 0)
			return baseMap[v+127];
		return new IntObject(v);
	}
	
	public static IntObject valueOf(long v){
		if (v+127 < 255 &&  v+127 > 0)
			return baseMap[(int) (v+127)];
		return new IntObject(v);
	}
	
	private IntObject(int v){
		value = v;
		newObject();
	}
	
	private IntObject(long v){
		value = v;
		newObject();
	}
	
	private long value;

	@Override
	public boolean truthValue() {
		return !(value == 0);
	}
	
	public long intValue(){
		return value;
	}
	
	@Override
	public double getJavaFloat() {
		return (double)value;
	}
	
	@Override
	public int getId(){
		return hashCode();
	}

	@Override
	protected PythonObject getIntValue() {
		return this;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (value ^ (value >>> 32));
		return result;
	}

	@Override
	protected String doToString() {
		return ""+value;
	}

	@Override
	public Long getJavaInt() {
		return new Long(value);
	}
}
