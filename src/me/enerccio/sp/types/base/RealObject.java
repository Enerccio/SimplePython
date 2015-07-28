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

/**
 * Represents doubles as real objects
 * @author Enerccio
 *
 */
public class RealObject extends NumberObject {
	private static final long serialVersionUID = 8L;
	
	public RealObject(){
		newObject();
	}

	public RealObject(double v){
		value = v;
		newObject();
	}
	
	public RealObject(float v){
		value = v;
		newObject();
	}
	
	private double value;
	
	@Override 
	public BigInteger getJavaInt() {
		return BigInteger.valueOf((long)value);
	}
	
	@Override
	public double getJavaFloat() {
		return value;
	}

	@Override
	public boolean truthValue() {
		return value != 0;
	}

	@Override
	public int getId(){
		return Double.valueOf(value).hashCode();
	}

	public float floatValue() {
		return (float)doubleValue();
	}
	
	public double doubleValue(){
		return value;
	}

	@Override
	protected PythonObject getIntValue() {
		return IntObject.valueOf(new Double(value).longValue());
	}
	
	@Override
	public int hashCode(){
		return new Double(value).hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if (o instanceof RealObject)
			return value == ((RealObject)o).value;
		return false;
	}

	@Override
	protected String doToString() {
		return new Double(value).toString();
	}
}
