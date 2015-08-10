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
	private final NumberObject intRepresentation;
	
	@Override public NumberType getNumberType() { return NumberType.BOOL; }
	
	private BoolObject(boolean v){
		this.value = v;
		intRepresentation = NumberObject.valueOf(this.intValue());
		newObject();
	}
	
	@Override
	protected void registerObject(){
		
	}
	
	@Override
	public boolean truthValue() {
		return value;
	}
	

	@Override public int intValue() { return value ? 1 : 0; }
	@Override public long longValue() { return value ? 1l : 0l; }
	@Override public float floatValue() { return value ? 1.f : 0.f; }
	@Override public double doubleValue() { return value ? 1.0 : 0.0; }

	public static PythonObject fromBoolean(Boolean ret) {
		return ret ? TRUE : FALSE;
	}

	@Override
	protected String doToString() {
		return value ? "True" : "False";
	}

	public PythonObject not() {
		return value ? FALSE : TRUE;
	}
	
	@Override
	public PythonObject add(PythonObject b){
		return intRepresentation.add(b);
	}
	
	@Override
	public PythonObject sub(PythonObject b){
		return intRepresentation.sub(b);
	}
	
	@Override
	public PythonObject mul(PythonObject b){
		return intRepresentation.mul(b);

	}
	
	@Override
	public PythonObject div(PythonObject b){
		return intRepresentation.div(b);

	}
	
	@Override
	public PythonObject mod(PythonObject b){
		return intRepresentation.mod(b);

	}
	
	@Override
	public PythonObject and(PythonObject b){
		return intRepresentation.and(b);

	}
	
	@Override
	public PythonObject or(PythonObject b){
		return intRepresentation.or(b);

	}
	
	@Override
	public PythonObject xor(PythonObject b){
		return intRepresentation.xor(b);

	}
	
	@Override
	public PythonObject neg(){
		return intRepresentation.neg();
	}
	
	@Override
	public PythonObject pow(PythonObject b){
		return intRepresentation.add(b);
	}
	
	@Override
	public PythonObject ls(PythonObject b){
		return intRepresentation.ls(b);
	}
	
	@Override
	public PythonObject rs(PythonObject b){
		return intRepresentation.rs(b);
	}
	
	@Override
	public PythonObject lt(PythonObject b) {
		return intRepresentation.lt(b);
	}

	@Override
	public PythonObject le(PythonObject b) {
		return intRepresentation.le(b);
	}

	@Override
	public PythonObject eq(PythonObject b) {
		if (b == this) return BoolObject.TRUE;
		return intRepresentation.eq(b);
	}

	@Override
	public PythonObject ne(PythonObject b) {
		if (b == this) return BoolObject.FALSE;
		return intRepresentation.ne(b);

	}

	@Override
	public PythonObject gt(PythonObject b) {
		return intRepresentation.gt(b);
	}

	@Override
	public PythonObject ge(PythonObject b) {
		return intRepresentation.ge(b);
	}
}
