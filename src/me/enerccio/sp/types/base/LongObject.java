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

import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.sequences.StringObject;

/** Used only if PythonRuntime.USE_JAVA_INTEGERS is set to True */
public class LongObject extends NumberObject {
	private static final long serialVersionUID = 35L;
	private final long value;
	
	@Override public NumberType getNumberType() { return NumberType.LONG; }

	@Override
	public void newObject(){
		super.newObject();
	}
	
	LongObject(long v){
		value = v;
		newObject();
	}
	
	@Override
	public boolean truthValue() {
		return !(value == 0);
	}
	
	@Override
	public int getId(){
		return hashCode();
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

	@Override public int intValue() { return (int)value; }
	@Override public long longValue() { return value; }
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
				case FLOAT:
					return n.add(this);
				case LONG:
				case INT:
				case BOOL:
					return NumberObject.valueOf(value + n.longValue());
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
				case FLOAT:
					return n.add(new LongObject(-value));
				case LONG:
				case INT:
				case BOOL:
					return NumberObject.valueOf(value - n.longValue());
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
				case FLOAT:
					return n.mul(this);
				case LONG:
				case INT:
				case BOOL:
					return NumberObject.valueOf(value * n.longValue());
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
					if (PythonRuntime.USE_DOUBLE_FLOAT)
						return NumberObject.valueOf(value / n.doubleValue());
					else
						return NumberObject.valueOf(value / n.floatValue());
				case LONG:
				case INT:
				case BOOL:
					return NumberObject.valueOf(value / n.longValue());
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
					if (PythonRuntime.USE_DOUBLE_FLOAT)
						return NumberObject.valueOf(value % n.doubleValue());
					else
						return NumberObject.valueOf(value % n.floatValue());
				case LONG:
				case INT:
				case BOOL:
					return NumberObject.valueOf(value % n.longValue());
			}
		}
		return invalidOperation("%", b);
	}
	
	@Override
	public PythonObject and(PythonObject b){
		if (b instanceof NumberObject) {
			NumberObject n = (NumberObject)b;
			switch (n.getNumberType()) {
				case COMPLEX:
				case FLOAT:
					return invalidOperation("&", b);
				case LONG:
				case INT:
				case BOOL:
					return NumberObject.valueOf(value & n.longValue());
			}
		}
		return invalidOperation("&", b);
	}
	
	@Override
	public PythonObject or(PythonObject b){
		if (b instanceof NumberObject) {
			NumberObject n = (NumberObject)b;
			switch (n.getNumberType()) {
				case COMPLEX:
				case FLOAT:
					return invalidOperation("|", b);
				case LONG:
				case INT:
				case BOOL:
					return NumberObject.valueOf(value | n.longValue());
			}
		}
		return invalidOperation("|", b);
	}
	
	@Override
	public PythonObject xor(PythonObject b){
		if (b instanceof NumberObject) {
			NumberObject n = (NumberObject)b;
			switch (n.getNumberType()) {
				case COMPLEX:
				case FLOAT:
					return invalidOperation("^", b);
				case LONG:
				case INT:
				case BOOL:
					return NumberObject.valueOf(value ^ n.longValue());
			}
		}
		return invalidOperation("^", b);
	}
	
	@Override
	public PythonObject neg(){
		return new LongObject(~value);
	}
	
	@Override
	public PythonObject pow(PythonObject b){
		if (b instanceof NumberObject) {
			NumberObject n = (NumberObject)b;
			switch (n.getNumberType()) {
				case COMPLEX:
					return invalidOperation("** or pow()", b);
				case FLOAT:
					return NumberObject.valueOf(Math.pow(value, n.doubleValue()));
				case LONG:
				case INT:
				case BOOL:
					return NumberObject.valueOf((long)Math.pow(value, n.doubleValue()));
			}
		}
		return invalidOperation("** or pow()", b);
	}
	
	@Override
	public PythonObject ls(PythonObject b){
		if (b instanceof NumberObject) {
			NumberObject n = (NumberObject)b;
			switch (n.getNumberType()) {
				case COMPLEX:
				case FLOAT:
					return invalidOperation("<<", b);
				case LONG:
				case INT:
				case BOOL:
					return NumberObject.valueOf(value << n.longValue());
			}
		}
		return invalidOperation("<<", b);
	}
	
	@Override
	public PythonObject rs(PythonObject b){
		if (b instanceof NumberObject) {
			NumberObject n = (NumberObject)b;
			switch (n.getNumberType()) {
				case COMPLEX:
				case FLOAT:
					return invalidOperation(">>", b);
				case LONG:
				case INT:
				case BOOL:
					return NumberObject.valueOf(value >> n.longValue());
			}
		}
		return invalidOperation(">>", b);
	}
	
	@Override
	public PythonObject lt(PythonObject b) {
		if (b instanceof NumberObject) {
			NumberObject n = (NumberObject)b;
			switch (n.getNumberType()) {
				case COMPLEX:
				case FLOAT:
					return ((BoolObject)n.ge(this)).not();
				case LONG:
				case INT:
				case BOOL:
					return value < n.longValue() ? BoolObject.TRUE : BoolObject.FALSE;
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
				case FLOAT:
					return ((BoolObject)n.gt(this)).not();
				case LONG:
				case INT:
				case BOOL:
					return value <= n.longValue() ? BoolObject.TRUE : BoolObject.FALSE;
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
				case FLOAT:
					return n.eq(this);
				case LONG:
				case INT:
				case BOOL:
					return value == n.longValue() ? BoolObject.TRUE : BoolObject.FALSE;
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
				case FLOAT:
					return n.ne(this);
				case LONG:
				case INT:
				case BOOL:
					return value != n.longValue() ? BoolObject.TRUE : BoolObject.FALSE;
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
				case FLOAT:
					return ((BoolObject)n.le(this)).not();
				case LONG:
				case INT:
				case BOOL:
					return value > n.longValue() ? BoolObject.TRUE : BoolObject.FALSE;
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
				case FLOAT:
					return ((BoolObject)n.lt(this)).not();
				case LONG:
				case INT:
				case BOOL:
					return value >= n.longValue() ? BoolObject.TRUE : BoolObject.FALSE;
			}
		}
		return BoolObject.FALSE;
	}
}
