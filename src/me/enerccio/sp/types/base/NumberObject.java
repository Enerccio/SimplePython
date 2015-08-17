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

import me.enerccio.sp.errors.AttributeError;
import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.interpret.InterpreterMathExecutorHelper.*;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.utils.Utils;

/**
 * Root number object. Represents all numbers
 * @author Enerccio
 *
 */
public abstract class NumberObject extends PythonObject 
	implements HasAddMethod, HasAndMethod, HasDivMethod, HasEqMethod, HasNeMethod, HasGeMethod, HasGtMethod, HasLeMethod, HasLshiftMethod, HasLtMethod,
			   HasModMethod, HasMulMethod, HasOrMethod, HasPowMethod, HasRshiftMethod, HasSubMethod, HasXorMethod
			{
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

	public NumberObject() { 
		super(false);
	}
	
	@Override protected void registerObject(){ }
	
	public static NumberObject valueOf(int n) {
		return IntObject.getCached(n);
	}
	
	public static NumberObject valueOf(long n) {
		if (n >= Integer.MIN_VALUE && n <= Integer.MAX_VALUE)
			return IntObject.getCached((int)n);
		if (PythonRuntime.USE_INT_ONLY)
			throw new TypeError("Integer overflow");
		return new LongObject(n);
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

	/** Returns True if specified PythonObject is NumberObject and int */ 
	public static boolean isInteger(PythonObject o) {
		if (o instanceof NumberObject)
			if (((NumberObject)o).getNumberType() == NumberType.INT)
				return true;
		return false;
	}
	
	/** 
	 * Returns True if specified PythonObject is NumberObject and long.
	 * If PythonRuntime.USE_INT_ONLY is set to true, returns true for ints as well. 
	 */ 
	public static boolean isLong(PythonObject o) {
		if (o instanceof NumberObject) {
			if (((NumberObject)o).getNumberType() == NumberType.LONG)
				return true;
			if (PythonRuntime.USE_INT_ONLY && ((NumberObject)o).getNumberType() == NumberType.INT)
				return true;
		}
		return false;
	}

	/** 
	 * Returns True if specified PythonObject is NumberObject and either int or long.
	 * If PythonRuntime.USE_INT_ONLY is set to true, returns true for ints as well. 
	 */ 
	public static boolean isFixed(PythonObject o) {
		if (o instanceof NumberObject) {
			if (((NumberObject)o).getNumberType() == NumberType.LONG)
				return true;
			if (((NumberObject)o).getNumberType() == NumberType.INT)
				return true;
		}
		return false;
	}
	/** 
	 * Returns True if specified PythonObject is NumberObject and float.
	 */ 
	public static boolean isFloat(PythonObject o) {
		if (o instanceof NumberObject)
			if (((NumberObject)o).getNumberType() == NumberType.FLOAT)
				return true;
		return false;
	}

	private static Map<String, JavaMethodObject> sfields = new HashMap<String, JavaMethodObject>();
	
	static {
		try {
			sfields.putAll(PythonObject.getSFields());
			sfields.put(__RSHIFT__, new JavaMethodObject(NumberObject.class, "rshift", PythonObject.class));
			sfields.put(__LSHIFT__, new JavaMethodObject(NumberObject.class, "lshift", PythonObject.class));
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
			throw new AttributeError("'" + 
					Utils.run("str", Utils.run("typename", this)) + "' object has no attribute '" + key + "'");
		throw new AttributeError("'" + 
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
		throw new TypeError("bad operand type for unary ~: '" + this + "'");
	}
	
	public PythonObject pow(PythonObject b){
		return invalidOperation("** or pow()", b);
	}
	
	public PythonObject lshift(PythonObject b){
		return invalidOperation("<<", b);
	}
	
	public PythonObject rshift(PythonObject b){
		return invalidOperation(">>", b);
	}
	
	public abstract PythonObject lt(PythonObject arg);
	
	public abstract PythonObject le(PythonObject arg);
	
	public abstract PythonObject eq(PythonObject arg);
	
	public abstract PythonObject ne(PythonObject arg);
	
	public abstract PythonObject gt(PythonObject arg);
	
	public abstract PythonObject ge(PythonObject arg);

	private static class IntObject extends NumberObject {
		private static final long serialVersionUID = 6L;
		private final int value;

		@Override public NumberType getNumberType() { return NumberType.INT; }

		@Override
		public void newObject(){
			
		}
		
		private static IntObject[] baseMap = new IntObject[1 + 2 * PythonRuntime.PREALOCATED_INTEGERS];
		static {
			for (int i=-PythonRuntime.PREALOCATED_INTEGERS; i<PythonRuntime.PREALOCATED_INTEGERS; i++){
				baseMap[i + PythonRuntime.PREALOCATED_INTEGERS] = new IntObject(i);
			}
		}
		
		static IntObject getCached(int v){
			if (v > -PythonRuntime.PREALOCATED_INTEGERS &&  v < PythonRuntime.PREALOCATED_INTEGERS)
				return baseMap[v+PythonRuntime.PREALOCATED_INTEGERS];
			return new IntObject(v);
		}
		
		private IntObject(int v){
			value = v;
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
				return new StringObject("" + value + ((StringObject)b).value);
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
							return NumberObject.valueOf(value - n.longValue());
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
							long r = value * n.intValue();
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
		public PythonObject lshift(PythonObject b){
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
		public PythonObject rshift(PythonObject b){
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

	private static class LongObject extends NumberObject {
		private static final long serialVersionUID = 35L;
		private final long value;
		
		@Override public NumberType getNumberType() { return NumberType.LONG; }

		LongObject(long v){
			value = v;
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
				return new StringObject("" + value + ((StringObject)b).value);
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
		public PythonObject lshift(PythonObject b){
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
		public PythonObject rshift(PythonObject b){
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

	private static class FloatObject extends NumberObject {
		private static final long serialVersionUID = 8L;
		private final float value;
		
		@Override public NumberType getNumberType() { return NumberType.FLOAT; }

		FloatObject(float v){
			value = v;
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
				return new StringObject("" + value + ((StringObject)b).value);
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

	
	private static class DoubleObject extends NumberObject {
		private static final long serialVersionUID = 8L;
		private final double value;
		
		@Override public NumberType getNumberType() { return NumberType.FLOAT; }

		public DoubleObject(double v){
			value = v;
		}
		
		@Override
		public boolean truthValue() {
			return value != 0.0;
		}

		@Override
		public int getId(){
			return Double.valueOf(value).hashCode();
		}

		@Override
		public int hashCode(){
			return new Double(value).hashCode();
		}
		
		@Override
		public boolean equals(Object o){
			if (o instanceof DoubleObject)
				return value == ((DoubleObject)o).value;
			return false;
		}

		@Override
		protected String doToString() {
			return new Double(value).toString();
		}

		@Override public int intValue() { return (int)value; }
		@Override public long longValue() { return (long)value; }
		@Override public float floatValue() { return (float)value; }
		@Override public double doubleValue() { return value; }
		
		@Override
		public PythonObject add(PythonObject b){
			if (b instanceof StringObject)
				return new StringObject("" + value + ((StringObject)b).value);
			if (b instanceof NumberObject) {
				NumberObject n = (NumberObject)b;
				switch (n.getNumberType()) {
					case COMPLEX:
						return n.add(this);
					case FLOAT:
					case LONG:
					case INT:
					case BOOL:
						return NumberObject.valueOf(value + n.doubleValue());
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
						return n.add(new DoubleObject(-value));
					case FLOAT:
					case LONG:
					case INT:
					case BOOL:
						return NumberObject.valueOf(value - n.doubleValue());
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
						return NumberObject.valueOf(value * n.doubleValue());
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
						return NumberObject.valueOf(value / n.doubleValue());
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
						return NumberObject.valueOf(value % n.doubleValue());
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
						return NumberObject.valueOf(Math.pow(value, n.doubleValue()));
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
						return value < n.doubleValue() ? BoolObject.TRUE : BoolObject.FALSE;
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
						return value <= n.doubleValue() ? BoolObject.TRUE : BoolObject.FALSE;
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
						return value == n.doubleValue() ? BoolObject.TRUE : BoolObject.FALSE;
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
						return value != n.doubleValue() ? BoolObject.TRUE : BoolObject.FALSE;
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
						return value > n.doubleValue() ? BoolObject.TRUE : BoolObject.FALSE;
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
						return value >= n.doubleValue() ? BoolObject.TRUE : BoolObject.FALSE;
				}
			}
			return BoolObject.FALSE;
		}
	}

	
}
