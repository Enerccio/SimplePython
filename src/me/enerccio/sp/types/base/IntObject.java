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

import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.sequences.StringObject;

/** Used only if PythonRuntime.USE_JAVA_INTEGERS is set to True */
public class IntObject extends NumberObject {
	private static final long serialVersionUID = 6L;
	private final int value;

	@Override public NumberType getNumberType() { return NumberType.INT; }

	@Override
	public void newObject(){
		super.newObject();
	}
	
	private static IntObject[] baseMap = new IntObject[1 + 2 * PythonRuntime.PREALOCATED_INTEGERS];
	static {
		for (int i=-PythonRuntime.PREALOCATED_INTEGERS; i<PythonRuntime.PREALOCATED_INTEGERS; i++){
			baseMap[i + PythonRuntime.PREALOCATED_INTEGERS] = new IntObject(i);
			baseMap[i + PythonRuntime.PREALOCATED_INTEGERS].newObject();
		}
	}
	
	static IntObject getCached(int v){
		if (v > -PythonRuntime.PREALOCATED_INTEGERS &&  v < PythonRuntime.PREALOCATED_INTEGERS)
			return baseMap[v+PythonRuntime.PREALOCATED_INTEGERS];
		return new IntObject(v);
	}
	
	private IntObject(int v){
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

	@Override public int intValue() { return value; }
	@Override public long longValue() { return value; }
	@Override public float floatValue() { return value; }
	@Override public double doubleValue() { return value; }
	
	public IntObject negative() {
		return IntObject.getCached(-value);
	}

	@Override
	public PythonObject add(PythonObject b){
		if (b instanceof StringObject)
			return new StringObject(((StringObject)b).value + value);
		if (b instanceof NumberObject) {
			NumberObject n = (NumberObject)b;
			switch (n.getNumberType()) {
				case COMPLEX:
				case FLOAT:
				case LONG:
					return n.add(this);
				case BOOL:
				case INT:
					if (PythonRuntime.USE_INT_ONLY) {
						int r = value + n.intValue();
						if (((value ^ r) & (n.intValue() ^ r)) < 0) {
							throw new TypeError("int overflow");
						}
						return IntObject.getCached(r);
					} else {
						return NumberObject.valueOf(value + n.longValue());
					}
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
				case LONG:
				case FLOAT:
					return n.add(negative());
				case BOOL:
				case INT:
					if (PythonRuntime.USE_INT_ONLY) {
						int r = value - n.intValue();
						if (((value ^ n.intValue()) & (value ^ r)) < 0) {
							throw new TypeError("int overflow");
						}
						return IntObject.getCached(r);
					} else {
						return NumberObject.valueOf(value + n.longValue());
					}
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
				case LONG:
					return n.mul(this);
				case BOOL:
				case INT:
					if (PythonRuntime.USE_INT_ONLY) {
						long r = value + n.intValue();
						int ir = (int)r;
						if (ir != r)
							throw new TypeError("int overflow");
    					return IntObject.getCached(ir);
					} else {
						return NumberObject.valueOf(value + n.longValue());
					}
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
					return NumberObject.valueOf(value / n.longValue());
				case BOOL:
				case INT:
    				return IntObject.getCached(value / n.intValue());
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
					return NumberObject.valueOf(value % n.longValue());
				case BOOL:
				case INT:
    				return IntObject.valueOf(value % n.intValue());
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
					return NumberObject.valueOf(value & n.longValue());
				case BOOL:
				case INT:
    				return IntObject.getCached(value & n.intValue());
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
					return NumberObject.valueOf(value | n.longValue());
				case BOOL:
				case INT:
    				return IntObject.getCached(value | n.intValue());
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
					return NumberObject.valueOf(value ^ n.longValue());
				case BOOL:
				case INT:
    				return IntObject.getCached(value ^ n.intValue());
			}
		}
		return invalidOperation("^", b);
	}
	
	@Override
	public PythonObject neg(){
		return IntObject.getCached(~value);
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
					return NumberObject.valueOf((long)Math.pow(value, n.doubleValue()));
				case BOOL:
				case INT:
					long r = (long)Math.pow(value, n.doubleValue());
					if (PythonRuntime.USE_INT_ONLY) {
						int ir = (int)r;
						if (ir != r)
							throw new TypeError("int overflow");
						return IntObject.getCached(ir);
					}						
					return NumberObject.valueOf(r);
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
					return NumberObject.valueOf(value << n.longValue());
				case BOOL:
				case INT:
					long r = (long)value << n.longValue();
					if (PythonRuntime.USE_INT_ONLY) {
						int ir = (int)r;
						if (ir != r)
							throw new TypeError("int overflow");
						return IntObject.getCached(ir);
					}						
					return NumberObject.valueOf(r);
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
					return NumberObject.valueOf(value >> n.longValue());
				case BOOL:
				case INT:
					long r = (long)value >> n.longValue();
					if (PythonRuntime.USE_INT_ONLY) {
						int ir = (int)r;
						if (ir != r)
							throw new TypeError("int overflow");
						return IntObject.getCached(ir);
					}						
					return NumberObject.valueOf(r);
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
				case LONG:
					return ((BoolObject)n.ge(this)).not();
				case BOOL:
				case INT:
					return value < n.intValue() ? BoolObject.TRUE : BoolObject.FALSE;
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
				case LONG:
					return ((BoolObject)n.gt(this)).not();
				case BOOL:
				case INT:
					return value <= n.intValue() ? BoolObject.TRUE : BoolObject.FALSE;
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
				case LONG:
					return n.eq(this);
				case BOOL:
				case INT:
					return value == n.intValue() ? BoolObject.TRUE : BoolObject.FALSE;
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
				case LONG:
					return n.ne(this);
				case BOOL:
				case INT:
					return value != n.intValue() ? BoolObject.TRUE : BoolObject.FALSE;
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
				case LONG:
					return ((BoolObject)n.le(this)).not();
				case BOOL:
				case INT:
					return value > n.intValue() ? BoolObject.TRUE : BoolObject.FALSE;
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
				case LONG:
					return ((BoolObject)n.lt(this)).not();
				case BOOL:
				case INT:
					return value >= n.intValue() ? BoolObject.TRUE : BoolObject.FALSE;
			}
		}
		return BoolObject.FALSE;
	}
}
