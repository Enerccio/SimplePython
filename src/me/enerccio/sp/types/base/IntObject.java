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

import java.math.BigInteger;

import me.enerccio.sp.types.PythonObject;

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
		value = BigInteger.valueOf(v);
		newObject();
	}
	
	private IntObject(long v){
		value = BigInteger.valueOf(v);
		newObject();
	}
	
	public IntObject(BigInteger v){
		value = v;
		newObject();
	}
	
	private BigInteger value;

	@Override
	public boolean truthValue() {
		return !value.equals(0);
	}
	
	@Override 
	public BigInteger getJavaInt() {
		return value;
	}
	
	@Override
	public double getJavaFloat() {
		return value.doubleValue();
	}
	
	@Override
	public int getId(){
		return value.hashCode();
	}

	public int intValue() {
		return (int)longValue();
	}

	public long longValue() {
		return value.longValue();
	}

	@Override
	protected PythonObject getIntValue() {
		return this;
	}
	
	@Override
	public int hashCode(){
		return value.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if (o instanceof IntObject)
			return value.equals(((IntObject)o).value);
		return false;
	}

	@Override
	protected String doToString() {
		return value.toString();
	}
}
