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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.utils.Utils;

/**
 * Root number object. Represents all numbers
 * @author Enerccio
 *
 */
public abstract class NumberObject extends PythonObject {
	private static final long serialVersionUID = 8168239961379175666L;
	public enum NumberType {
		BOOL, INT, LONG, FLOAT, COMPLEX, 
	}
	
	public static final String __INT__ = "__int__";
	// Arithmetics
	public static final String __ADD__ = "__add__";
	public static final String __SUB__ = "__sub__";
	public static final String __MUL__ = "__mul__";
	public static final String __DIV__ = "__div__";
	public static final String __MOD__ = "__mod__";
	public static final String __POW__ = "__pow__";
	// Bitwise stuff
	public static final String __AND__ = "__and__";
	public static final String __OR__  = "__or__";
	public static final String __NEG__ = "__neg__";
	public static final String __XOR__ = "__xor__";
	public static final String __LSHIFT__ = "__lshift__";
	public static final String __RSHIFT__ = "__rshift__";
	// Comparisons
	public static final String __LT__ = "__lt__";
	public static final String __LE__ = "__le__";
	public static final String __GT__ = "__gt__";
	public static final String __GE__ = "__ge__";

	public NumberObject() { }
	@Override protected void registerObject(){ }
	
	public static NumberObject valueOf(int n) {
		return IntObject.getCached(n);
	}
	
	public static NumberObject valueOf(long n) {
		if (n >= Integer.MIN_VALUE && n <= Integer.MAX_VALUE) {
			if (PythonRuntime.USE_INT_ONLY)
				throw new TypeError("Integer overflow");
			return new LongObject(n);
		}
		return IntObject.getCached((int)n);
	}
	
	public static NumberObject valueOf(double d) {
		if (PythonRuntime.USE_DOUBLE_FLOAT)
			return new DoubleObject(d);
		return new FloatObject((float)d);
	}
	
	public static NumberObject valueOf(float f) {
		if (PythonRuntime.USE_DOUBLE_FLOAT)
			return new DoubleObject(f);
		return new FloatObject(f);
	}
	
	public static boolean isInteger(PythonObject o) {
		if (o instanceof NumberObject)
			if (((NumberObject)o).getNumberType() == NumberType.INT)
				return true;
		return false;
	}
	
	private static Map<String, JavaMethodObject> sfields = new HashMap<String, JavaMethodObject>();
	
	static {
		try {
			sfields.putAll(PythonObject.getSFields());
			sfields.put(__RSHIFT__, new JavaMethodObject(NumberObject.class, "rs", PythonObject.class));
			sfields.put(__LSHIFT__, new JavaMethodObject(NumberObject.class, "ls", PythonObject.class));
			sfields.put(__ADD__, new JavaMethodObject(NumberObject.class, "add", PythonObject.class));
			sfields.put(__SUB__, new JavaMethodObject(NumberObject.class, "sub", PythonObject.class));
			sfields.put(__MUL__, new JavaMethodObject(NumberObject.class, "mul", PythonObject.class));
			sfields.put(__DIV__, new JavaMethodObject(NumberObject.class, "div", PythonObject.class));
			sfields.put(__MOD__, new JavaMethodObject(NumberObject.class, "mod", PythonObject.class));
			sfields.put(__AND__, new JavaMethodObject(NumberObject.class, "and", PythonObject.class));
			sfields.put(__OR__,  new JavaMethodObject(NumberObject.class, "or", PythonObject.class));
			sfields.put(__XOR__, new JavaMethodObject(NumberObject.class, "xor", PythonObject.class));
			sfields.put(__POW__, new JavaMethodObject(NumberObject.class, "pow", PythonObject.class));
			sfields.put(__LT__, new JavaMethodObject(NumberObject.class, "lt", PythonObject.class));
			sfields.put(__LE__, new JavaMethodObject(NumberObject.class, "le", PythonObject.class));
			sfields.put(__EQ__, new JavaMethodObject(NumberObject.class, "eq", PythonObject.class));
			sfields.put(__NE__, new JavaMethodObject(NumberObject.class, "ne", PythonObject.class));
			sfields.put(__GE__, new JavaMethodObject(NumberObject.class, "ge", PythonObject.class));
			sfields.put(__GT__, new JavaMethodObject(NumberObject.class, "gt", PythonObject.class));

		} catch (Exception e) {
			throw new RuntimeException("Fuck", e);
		}
	}
	
	protected static Map<String, JavaMethodObject> getSFields(){ return sfields; }
	@Override
	public Set<String> getGenHandleNames() {
		return sfields.keySet();
	}

	@Override
	protected Map<String, JavaMethodObject> getGenHandles() {
		return sfields;
	}
	
	@Override
	public void newObject() {	
		super.newObject();
	};
	
	/** Throws TypeError if number is not fixed or is too big to be converted to int */ 
	public abstract int intValue();
	/** Throws TypeError if number is not fixed */ 
	public abstract long longValue();
	public abstract float floatValue();
	public abstract double doubleValue();
	public abstract NumberType getNumberType();
	
	public double getRealValue() { return doubleValue(); }
	public double getImaginaryValue() { return 0.0; }

	@Override
	public PythonObject set(String key, PythonObject localContext,
			PythonObject value) {
		if (!fields.containsKey(key))
			throw Utils.throwException("AttributeError", "'" + 
					Utils.run("str", Utils.run("typename", this)) + "' object has no attribute '" + key + "'");
		throw Utils.throwException("AttributeError", "'" + 
				Utils.run("str", Utils.run("typename", this)) + "' object attribute '" + key + "' is read only");
	}

	@Override
	public void create(String key, AccessRestrictions restrictions, PythonObject localContext) {
		
	}
	
	protected PythonObject invalidOperation(String op, PythonObject other) {
		throw new TypeError("unsupported operand type(s) for " + op + ": '" + this + "' and '" + other + "'");
	}
	
	public PythonObject add(PythonObject b){
		return invalidOperation("+", b);
	}
	
	public PythonObject sub(PythonObject b){
		return invalidOperation("-", b);
	}
	
	public PythonObject mul(PythonObject b){
		return invalidOperation("*", b);
	}
	
	public PythonObject div(PythonObject b){
		return invalidOperation("/", b);
	}
	
	public PythonObject mod(PythonObject b){
		return invalidOperation("%", b);
	}
	
	public PythonObject and(PythonObject b){
		return invalidOperation("&", b);
	}
	
	public PythonObject or(PythonObject b){
		return invalidOperation("|", b);
	}
	
	public PythonObject xor(PythonObject b){
		return invalidOperation("^", b);
	}
	
	public PythonObject neg(){
		throw Utils.throwException("TypeError", "bad operand type for unary ~: '" + this + "'");
	}
	
	public PythonObject pow(PythonObject b){
		return invalidOperation("** or pow()", b);
	}
	
	public PythonObject ls(PythonObject b){
		return invalidOperation("<<", b);
	}
	
	public PythonObject rs(PythonObject b){
		return invalidOperation(">>", b);
	}
	
	public abstract PythonObject lt(PythonObject arg);
	
	public abstract PythonObject le(PythonObject arg);
	
	public abstract PythonObject eq(PythonObject arg);
	
	public abstract PythonObject ne(PythonObject arg);
	
	public abstract PythonObject gt(PythonObject arg);
	
	public abstract PythonObject ge(PythonObject arg);
}
