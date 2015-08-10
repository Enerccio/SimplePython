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
import me.enerccio.sp.types.sequences.StringObject;

public class FloatObject extends NumberObject {
	private static final long serialVersionUID = 8L;
	private final float value;
	
	@Override public NumberType getNumberType() { return NumberType.FLOAT; }

	FloatObject(float v){
		value = v;
		newObject();
	}
	
	@Override
	public boolean truthValue() {
		return value != 0.0f;
	}

	@Override
	public int getId(){
		return Float.valueOf(value).hashCode();
	}

	@Override
	public int hashCode(){
		return new Float(value).hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if (o instanceof FloatObject)
			return value == ((FloatObject)o).value;
		return false;
	}

	@Override
	protected String doToString() {
		return new Float(value).toString();
	}

	@Override public int intValue() { return (int)value; }
	@Override public long longValue() { return (long)value; }
	@Override public float floatValue() { return value; }
	@Override public double doubleValue() { return value; }
	
	@Override
	public PythonObject add(PythonObject b){
		if (b instanceof StringObject)
			return new StringObject(((StringObject)b).value + value);
		if (b instanceof NumberObject) {
			NumberObject n = (NumberObject)b;
			switch (n.getNumberType()) {
				case COMPLEX:
					return n.add(this);
				case FLOAT:
				case LONG:
				case INT:
				case BOOL:
					return NumberObject.valueOf(value + n.floatValue());
			}
		}
		return invalidOperation("+", b);
	}
	
	@Override
	public PythonObject sub(PythonObject b){
		if (b instanceof NumberObject) {
			NumberObject n = (NumberObject)b;
			switch (n.getNumberType()) {
				case COMPLEX:
					return n.add(new FloatObject(-value));
				case FLOAT:
				case LONG:
				case INT:
				case BOOL:
					return NumberObject.valueOf(value - n.floatValue());
			}
		}
		return invalidOperation("-", b);
	}
	
	@Override
	public PythonObject mul(PythonObject b){
		if (b instanceof StringObject)
			return ((StringObject)b).mul(this);
		if (b instanceof NumberObject) {
			NumberObject n = (NumberObject)b;
			switch (n.getNumberType()) {
				case COMPLEX:
					return n.mul(this);
				case FLOAT:
				case LONG:
				case INT:
				case BOOL:
					return NumberObject.valueOf(value * n.floatValue());
			}
		}
		return invalidOperation("*", b);
	}
	
	@Override
	public PythonObject div(PythonObject b){
		if (b instanceof NumberObject) {
			NumberObject n = (NumberObject)b;
			switch (n.getNumberType()) {
				case COMPLEX:
					return invalidOperation("/", b);
				case FLOAT:
				case LONG:
				case INT:
				case BOOL:
					return NumberObject.valueOf(value / n.floatValue());
			}
		}
		return invalidOperation("/", b);
	}
	
	@Override
	public PythonObject mod(PythonObject b){
		if (b instanceof NumberObject) {
			NumberObject n = (NumberObject)b;
			switch (n.getNumberType()) {
				case COMPLEX:
					return invalidOperation("%", b);
				case FLOAT:
				case LONG:
				case INT:
				case BOOL:
					return NumberObject.valueOf(value % n.floatValue());
			}
		}
		return invalidOperation("%", b);
	}
	
	@Override
	public PythonObject pow(PythonObject b){
		if (b instanceof NumberObject) {
			NumberObject n = (NumberObject)b;
			switch (n.getNumberType()) {
				case COMPLEX:
					return invalidOperation("** or pow()", b);
				case FLOAT:
				case LONG:
				case INT:
				case BOOL:
					return NumberObject.valueOf(Math.pow(value, n.floatValue()));
			}
		}
		return invalidOperation("** or pow()", b);
	}
	
	@Override
	public PythonObject lt(PythonObject b) {
		if (b instanceof NumberObject) {
			NumberObject n = (NumberObject)b;
			switch (n.getNumberType()) {
				case COMPLEX:
					return ((BoolObject)n.ge(this)).not();
				case FLOAT:
				case LONG:
				case INT:
				case BOOL:
					return value < n.floatValue() ? BoolObject.TRUE : BoolObject.FALSE;
			}
		}
		return BoolObject.FALSE;
	}

	@Override
	public PythonObject le(PythonObject b) {
		if (b instanceof NumberObject) {
			NumberObject n = (NumberObject)b;
			switch (n.getNumberType()) {
				case COMPLEX:
					return ((BoolObject)n.gt(this)).not();
				case FLOAT:
				case LONG:
				case INT:
				case BOOL:
					return value <= n.floatValue() ? BoolObject.TRUE : BoolObject.FALSE;
			}
		}
		return BoolObject.FALSE;
	}

	@Override
	public PythonObject eq(PythonObject b) {
		if (b instanceof NumberObject) {
			NumberObject n = (NumberObject)b;
			switch (n.getNumberType()) {
				case COMPLEX:
					return n.eq(this);
				case FLOAT:
				case LONG:
				case INT:
				case BOOL:
					return value == n.floatValue() ? BoolObject.TRUE : BoolObject.FALSE;
			}
		}
		return BoolObject.FALSE;
	}

	@Override
	public PythonObject ne(PythonObject b) {
		if (b instanceof NumberObject) {
			NumberObject n = (NumberObject)b;
			switch (n.getNumberType()) {
				case COMPLEX:
					return n.ne(this);
				case FLOAT:
				case LONG:
				case INT:
				case BOOL:
					return value != n.floatValue() ? BoolObject.TRUE : BoolObject.FALSE;
			}
		}
		return BoolObject.FALSE;
	}

	@Override
	public PythonObject gt(PythonObject b) {
		if (b instanceof NumberObject) {
			NumberObject n = (NumberObject)b;
			switch (n.getNumberType()) {
				case COMPLEX:
					return ((BoolObject)n.le(this)).not();
				case FLOAT:
				case LONG:
				case INT:
				case BOOL:
					return value > n.floatValue() ? BoolObject.TRUE : BoolObject.FALSE;
			}
		}
		return BoolObject.FALSE;
	}

	@Override
	public PythonObject ge(PythonObject b) {
		if (b instanceof NumberObject) {
			NumberObject n = (NumberObject)b;
			switch (n.getNumberType()) {
				case COMPLEX:
					return ((BoolObject)n.lt(this)).not();
				case FLOAT:
				case LONG:
				case INT:
				case BOOL:
					return value >= n.floatValue() ? BoolObject.TRUE : BoolObject.FALSE;
			}
		}
		return BoolObject.FALSE;
	}
}
