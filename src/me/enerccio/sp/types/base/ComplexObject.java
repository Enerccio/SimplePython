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

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.utils.Utils;

/**
 * Complex Number type
 * @author Enerccio
 *
 */
public class ComplexObject extends NumberObject {
	private static final long serialVersionUID = 9L;
	private static final String REAL_ACCESSOR = "real";
	private static final String IMAG_ACCESSOR = "imag";
	
	public ComplexObject(){
		newObject();
	}
	
	public ComplexObject(double r, double i){
		this(new RealObject(r), new RealObject(i));
	}
	
	public ComplexObject(RealObject r, RealObject i) {
		fields.put(REAL_ACCESSOR, new AugumentedPythonObject(r, AccessRestrictions.PUBLIC));
		fields.put(IMAG_ACCESSOR, new AugumentedPythonObject(i, AccessRestrictions.PUBLIC));
		newObject();
	}
	
	@Override
	protected void registerObject(){
		
	}
	
	@Override 
	public Long getJavaInt() {
		throw Utils.throwException("TypeError", "can't convert complex to int");
	}
	
	@Override
	public double getJavaFloat() {
		throw Utils.throwException("TypeError", "can't convert complex to float");
	}

	@Override
	public boolean truthValue() {
		return Utils.get(this, REAL_ACCESSOR).truthValue();
	}
	
	public double getRealPart(){
		return ((RealObject) Utils.get(this, REAL_ACCESSOR)).getJavaFloat();
	}
	
	public double getImagPart(){
		return ((RealObject) Utils.get(this, IMAG_ACCESSOR)).getJavaFloat();
	}

	@Override
	protected PythonObject getIntValue() {
		return Utils.get(this, REAL_ACCESSOR);
	}
	
	@Override
	public int hashCode(){
		return fields.get(REAL_ACCESSOR).object.hashCode() ^ fields.get(IMAG_ACCESSOR).object.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if (o instanceof ComplexObject){
			return ((ComplexObject)o).fields.get(REAL_ACCESSOR).object.equals(fields.get(REAL_ACCESSOR).object)
					&& ((ComplexObject)o).fields.get(IMAG_ACCESSOR).object.equals(fields.get(IMAG_ACCESSOR).object);
		}
		return false;
	}

	@Override
	protected String doToString() {
		return "(" + fields.get(REAL_ACCESSOR).object.toString() + "+" + fields.get(IMAG_ACCESSOR).object.toString() + "j)";
	}
}
