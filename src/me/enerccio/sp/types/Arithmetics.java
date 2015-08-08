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
package me.enerccio.sp.types;

import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.types.base.BoolObject;
import me.enerccio.sp.types.base.ClassInstanceObject;
import me.enerccio.sp.types.base.ComplexObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.base.NumberObject;
import me.enerccio.sp.types.base.RealObject;
import me.enerccio.sp.types.sequences.ListObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.utils.Utils;

/**
 * Artihmetic java functions stored here
 * @author Enerccio
 *
 */
public final class Arithmetics {

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
	public static final String __NOT__ = "__not__";
	public static final String __XOR__ = "__xor__";
	public static final String __LSHIFT__ = "__lshift__";
	public static final String __RSHIFT__ = "__rshift__";
	// Comparisons
	public static final String __LT__ = "__lt__";
	public static final String __LE__ = "__le__";
	public static final String __EQ__ = "__eq__";
	public static final String __NE__ = "__ne__";
	public static final String __GT__ = "__gt__";
	public static final String __GE__ = "__ge__";
	
	public static PythonObject doOperator(PythonObject a, PythonObject b, String m) {
		if (a instanceof ClassInstanceObject){
			if (b == null)
				return PythonInterpreter.interpreter.get().execute(false, Utils.get(a, m), null);
			else
				return PythonInterpreter.interpreter.get().execute(false, Utils.get(a, m), null, b);
		}
		
		if (a instanceof IntObject){
			return doOperatorInt((IntObject)a, b, m);
		}
		
		if (a instanceof RealObject){
			return doOperatorFloat((RealObject)a, b, m);
		}
		
		if (a instanceof ComplexObject){
			return doOperatorComplex((ComplexObject)a, b, m);
		}
		
		if (a instanceof StringObject){
			return doOperatorString((StringObject)a, b, m);
		}
		
		if (a instanceof BoolObject){
			return doOperatorBool((BoolObject)a, b, m);
		}
		
		if (a instanceof ListObject){
			// TODO
		}
		
		if (b != null)
			throw Utils.throwException("TypeError", "unknown operation " + m + " of types '" + Utils.run("typename", a) + "' and '" + Utils.run("typename", b) + "'");
		else
			throw Utils.throwException("TypeError", "unknown operation " + m + " of type '" + Utils.run("typename", a) + "'");
	}

	private static PythonObject doOperatorInt(IntObject a, PythonObject b, String m) {
		boolean isInt = b instanceof IntObject;
		boolean isFloat = b instanceof RealObject;
		boolean isComplex = b instanceof ComplexObject;
		boolean isNumber = b instanceof NumberObject;
		
		switch (m){
			case __ADD__:
				if (isInt)
					return IntObject.valueOf(a.intValue() + ((IntObject) b).intValue());
				if (isFloat)
					return new RealObject((double)a.intValue() + ((NumberObject) b).getJavaFloat());
				if (isNumber)
					return IntObject.valueOf(a.intValue() + ((NumberObject) b).getJavaInt().longValue());
				if (isComplex)
					return new ComplexObject((double)a.intValue() + ((ComplexObject) b).getRealPart(), ((ComplexObject) b).getImagPart());
				break;
			case __SUB__:
				if (isInt)
					return IntObject.valueOf(a.intValue() - ((IntObject) b).intValue());
				if (isFloat)
					return new RealObject((double)a.intValue() - ((NumberObject) b).getJavaFloat());
				if (isNumber)
					return IntObject.valueOf(a.intValue() - ((NumberObject) b).getJavaInt().longValue());
				if (isComplex)
					return new ComplexObject((double)a.intValue() - ((ComplexObject) b).getRealPart(), ((ComplexObject) b).getImagPart());
				break;
			case __MUL__:
				if (isInt)
					return IntObject.valueOf(a.intValue() * ((IntObject) b).intValue());
				if (isFloat)
					return new RealObject((double)a.intValue() * ((NumberObject) b).getJavaFloat());
				if (isNumber)
					return IntObject.valueOf(a.intValue() * ((NumberObject) b).getJavaInt().longValue());
				if (isComplex)
					return new ComplexObject((double)a.intValue() * ((ComplexObject) b).getRealPart(), ((ComplexObject) b).getImagPart());
				break;
			case __DIV__:
				if (isInt)
					return IntObject.valueOf(a.intValue() / ((IntObject) b).intValue());
				if (isFloat)
					return new RealObject((double)a.intValue() / ((NumberObject) b).getJavaFloat());
				if (isNumber)
					return IntObject.valueOf(a.intValue() / ((NumberObject) b).getJavaInt().longValue());
				if (isComplex)
					return new ComplexObject((double)a.intValue() / ((ComplexObject) b).getRealPart(), ((ComplexObject) b).getImagPart());
				break;
			case __MOD__:
				if (isInt)
					return IntObject.valueOf(a.intValue() % ((IntObject) b).intValue());
				if (isFloat)
					return new RealObject((double)a.intValue() % ((NumberObject) b).getJavaFloat());
				if (isNumber)
					return IntObject.valueOf(a.intValue() % ((NumberObject) b).getJavaInt().longValue());
				if (isComplex)
					return new ComplexObject((double)a.intValue() % ((ComplexObject) b).getRealPart(), ((ComplexObject) b).getImagPart());
				break;
			case __AND__:
				if (isFloat)
					break;
				if (isNumber)	// int and bool
					return IntObject.valueOf(a.intValue() & ((NumberObject) b).getJavaInt().longValue());
				break; // Unknown operation
			case __OR__:
				if (isFloat)
					break;
				if (isNumber)	// int and bool
					return IntObject.valueOf(a.intValue() | ((NumberObject) b).getJavaInt().longValue());
				break; // Unknown operation
			case __XOR__:
				if (isFloat)
					break;
				if (isNumber)	// int and bool
					return IntObject.valueOf(a.intValue() ^ ((NumberObject) b).getJavaInt().longValue());
				break; // Unknown operation
			case __NOT__:
				return IntObject.valueOf(~a.intValue());
			case __POW__:
				if (isFloat)
					return new RealObject(Math.pow((double)a.intValue(), ((RealObject) b).getJavaFloat()));
				if (isNumber)
					return IntObject.valueOf((int) Math.pow(a.intValue(),  ((IntObject) b).intValue()));
				if (isComplex)
					return new ComplexObject(Math.pow((double)a.intValue(), ((ComplexObject) b).getRealPart()), ((ComplexObject) b).getImagPart());
			case __RSHIFT__:
				if (isFloat)
					break;
				if (isNumber)	// int and bool
					return IntObject.valueOf(a.intValue() >> ((IntObject) b).intValue());
				break; // Unknown operation
			case __LSHIFT__:
				if (isFloat)
					break;
				if (isNumber)	// int and bool
					return IntObject.valueOf(a.intValue() << ((IntObject) b).intValue());
				break; // Unknown operation
			case __LT__ :
				if (isInt)
					return BoolObject.fromBoolean(a.getJavaInt() < ((NumberObject)b).getJavaInt());
				if (isFloat)
					return BoolObject.fromBoolean(a.getJavaFloat() < ((NumberObject)b).getJavaFloat());
				break; // Unknown operation
			case __GT__ :
				if (isInt)
					return BoolObject.fromBoolean(a.getJavaInt() > ((NumberObject)b).getJavaInt());
				if (isNumber)
					return BoolObject.fromBoolean(a.getJavaFloat() > ((NumberObject)b).getJavaFloat());
				break; // Unknown operation
			case __LE__ :
				if (isInt)
					return BoolObject.fromBoolean(a.getJavaInt() <= ((NumberObject)b).getJavaInt());
				if (isNumber)
					return BoolObject.fromBoolean(a.getJavaFloat() <= ((NumberObject)b).getJavaFloat());
				break; // Unknown operation
			case __GE__ :
				if (isInt)
					return BoolObject.fromBoolean(a.getJavaInt() >= ((NumberObject)b).getJavaInt());
				if (isNumber)
					return BoolObject.fromBoolean(a.getJavaFloat() >= ((NumberObject)b).getJavaFloat());
				break; // Unknown operation
			case __EQ__ :
				if (isInt)
					return BoolObject.fromBoolean(a.getJavaInt() == ((NumberObject)b).getJavaInt());
				if (isNumber)
					return BoolObject.fromBoolean(a.getJavaFloat() == ((NumberObject)b).getJavaFloat());
				return BoolObject.FALSE;
			case __NE__ :
				if (isInt)
					return BoolObject.fromBoolean(a.getJavaInt() != ((NumberObject)b).getJavaInt());
				if (isNumber)
					return BoolObject.fromBoolean(a.getJavaFloat() != ((NumberObject)b).getJavaFloat());
				return BoolObject.TRUE;
		}
		
		if (b != null)
			throw Utils.throwException("TypeError", "Unknown operation " + m + " of types '" + Utils.run("typename", a) + "' and '" + Utils.run("typename", b) + "'");
		else
			throw Utils.throwException("TypeError", "Unknown operation " + m + " of type '" + Utils.run("typename", a) + "'");
	}
 
	private static PythonObject doOperatorFloat(RealObject a, PythonObject b,
			String m) {
		boolean isInt = b instanceof IntObject;
		boolean isFloat = b instanceof RealObject;
		boolean isComplex = b instanceof ComplexObject;
		boolean isNumber = b instanceof NumberObject;
		
		switch (m){
			case __ADD__:
				if (isInt){
					return new RealObject(a.getJavaFloat() + ((IntObject) b).intValue());
				}
				if (isFloat){
					return new RealObject(a.getJavaFloat() + ((RealObject) b).getJavaFloat());
				}
				if (isComplex){
					return new ComplexObject(a.getJavaFloat() + ((ComplexObject) b).getRealPart(), ((ComplexObject) b).getImagPart());
				}
			case __SUB__:
				if (isInt){
					return new RealObject(a.getJavaFloat() - ((IntObject) b).intValue());
				}
				if (isFloat){
					return new RealObject(a.getJavaFloat() - ((RealObject) b).getJavaFloat());
				}
				if (isComplex){
					return new ComplexObject(a.getJavaFloat() - ((ComplexObject) b).getRealPart(), ((ComplexObject) b).getImagPart());
				}
			case __MUL__:
				if (isInt){
					return new RealObject(a.getJavaFloat() * ((IntObject) b).intValue());
				}
				if (isFloat){
					return new RealObject(a.getJavaFloat() * ((RealObject) b).getJavaFloat());
				}
				if (isComplex){
					return new ComplexObject(a.getJavaFloat() * ((ComplexObject) b).getRealPart(), ((ComplexObject) b).getImagPart());
				}
			case __DIV__:
				if (isInt){
					return new RealObject(a.getJavaFloat() / ((IntObject) b).intValue());
				}
				if (isFloat){
					return new RealObject(a.getJavaFloat() / ((RealObject) b).getJavaFloat());
				}
				if (isComplex){
					return new ComplexObject(a.getJavaFloat() / ((ComplexObject) b).getRealPart(), ((ComplexObject) b).getImagPart());
				}
			case __MOD__:
				if (isInt){
					return new RealObject(a.getJavaFloat() % ((IntObject) b).intValue());
				}
				if (isFloat){
					return new RealObject(a.getJavaFloat() % ((RealObject) b).getJavaFloat());
				}
				if (isComplex){
					return new ComplexObject(a.getJavaFloat() % ((ComplexObject) b).getRealPart(), ((ComplexObject) b).getImagPart());
				}
			case __AND__:
				break; // Unknown operation
			case __OR__:
				break; // Unknown operation
			case __XOR__:
				break; // Unknown operation
			case __NOT__:
				break; // Unknown operation
			case __POW__:
				if (isInt){
					return new RealObject(Math.pow(a.getJavaFloat(), ((IntObject) b).intValue()));
				}
				if (isFloat){
					return new RealObject(Math.pow(a.getJavaFloat(), ((RealObject) b).getJavaFloat()));
				}
				if (isComplex){
					return new ComplexObject(Math.pow(a.getJavaFloat(), ((ComplexObject) b).getRealPart()), ((ComplexObject) b).getImagPart());
				}

			case __LT__ :
				if (isNumber)
					return BoolObject.fromBoolean(a.getJavaFloat() < ((NumberObject)b).getJavaFloat());
				break; // Unknown operation
			case __GT__ :
				if (isNumber)
					return BoolObject.fromBoolean(a.getJavaFloat() > ((NumberObject)b).getJavaFloat());
				break; // Unknown operation
			case __LE__ :
				if (isNumber)
					return BoolObject.fromBoolean(a.getJavaFloat() <= ((NumberObject)b).getJavaFloat());
				break; // Unknown operation
			case __GE__ :
				if (isNumber)
					return BoolObject.fromBoolean(a.getJavaFloat() >= ((NumberObject)b).getJavaFloat());
				break; // Unknown operation
			case __EQ__ :
				if (isNumber)
					return BoolObject.fromBoolean(a.getJavaFloat() == ((NumberObject)b).getJavaFloat());
				return BoolObject.FALSE;
			case __NE__ :
				if (isNumber)
					return BoolObject.fromBoolean(a.getJavaFloat() != ((NumberObject)b).getJavaFloat());
				return BoolObject.TRUE;
			case __RSHIFT__:
				break; // Unknown operation
			case __LSHIFT__:
				break; // Unknown operation
		}
		
		if (b != null)
			throw Utils.throwException("TypeError", "unknown operation " + m + " of types '" + Utils.run("typename", a) + "' and '" + Utils.run("typename", b) + "'");
		else
			throw Utils.throwException("TypeError", "unknown operation " + m + " of type '" + Utils.run("typename", a) + "'");
	}
	
	private static PythonObject doOperatorComplex(ComplexObject a,
			PythonObject b, String m) {
		boolean isInt = b instanceof IntObject;
		boolean isFloat = b instanceof RealObject;
		boolean isComplex = b instanceof ComplexObject;
		
		switch (m){
			case __ADD__:
				if (isInt){
					return new ComplexObject(a.getRealPart() + ((IntObject)b).intValue(), a.getImagPart());
				}
				if (isFloat){
					return new ComplexObject(a.getRealPart() + ((RealObject)b).getJavaFloat(), a.getImagPart());
				}
				if (isComplex){
					return new ComplexObject(a.getRealPart() + ((ComplexObject)b).getRealPart(), 
							a.getImagPart() + ((ComplexObject)b).getImagPart());
				}
			case __SUB__:
				if (isInt){
					return new ComplexObject(a.getRealPart() - ((IntObject)b).intValue(), a.getImagPart());
				}
				if (isFloat){
					return new ComplexObject(a.getRealPart() - ((RealObject)b).getJavaFloat(), a.getImagPart());
				}
				if (isComplex){
					return new ComplexObject(a.getRealPart() - ((ComplexObject)b).getRealPart(), 
							 a.getImagPart() - ((ComplexObject)b).getImagPart());
				}
			case __MUL__:
				if (isInt){
					return new ComplexObject(a.getRealPart() * ((IntObject)b).intValue(), a.getImagPart());
				}
				if (isFloat){
					return new ComplexObject(a.getRealPart() * ((RealObject)b).getJavaFloat(), a.getImagPart());
				}
				if (isComplex){
					return new ComplexObject(a.getRealPart() * ((ComplexObject)b).getRealPart(), 
							 a.getImagPart() * ((ComplexObject)b).getImagPart());
				}
			case __DIV__:
				if (isInt){
					return new ComplexObject(a.getRealPart() / ((IntObject)b).intValue(), a.getImagPart());
				}
				if (isFloat){
					return new ComplexObject(a.getRealPart() / ((RealObject)b).getJavaFloat(), a.getImagPart());
				}
				if (isComplex){
					return new ComplexObject(a.getRealPart() / ((ComplexObject)b).getRealPart(), 
							 a.getImagPart() / ((ComplexObject)b).getImagPart());
				}
			case __MOD__:
				if (isInt){
					return new ComplexObject(a.getRealPart() % ((IntObject)b).intValue(), a.getImagPart());
				}
				if (isFloat){
					return new ComplexObject(a.getRealPart() % ((RealObject)b).getJavaFloat(), a.getImagPart());
				}
				if (isComplex){
					return new ComplexObject(a.getRealPart() % ((ComplexObject)b).getRealPart(), 
							 a.getImagPart() % ((ComplexObject)b).getImagPart());
				}
			case __AND__:
				break; // Unknown operation
			case __OR__:
				break; // Unknown operation
			case __XOR__:
				break; // Unknown operation
			case __NOT__:
				break; // Unknown operation
			case __POW__:
				if (isInt){
					return new ComplexObject(Math.pow(a.getRealPart(), ((IntObject)b).intValue()), a.getImagPart());
				}
				if (isFloat){
					return new ComplexObject(Math.pow(a.getRealPart(), ((RealObject)b).getJavaFloat()), a.getImagPart());
				}
				if (isComplex){
					return new ComplexObject(Math.pow(a.getRealPart(), ((ComplexObject)b).getRealPart()), 
							 Math.pow(a.getImagPart(), ((ComplexObject)b).getImagPart()));
				}
			case __EQ__ :
				return BoolObject.FALSE;
			case __NE__ :
				return BoolObject.TRUE;
			case __RSHIFT__:
				break; // Unknown operation
			case __LSHIFT__:
				break; // Unknown operation
		}
		
		if (b != null)
			throw Utils.throwException("TypeError", "unknown operation " + m + " of types '" + Utils.run("typename", a) + "' and '" + Utils.run("typename", b) + "'");
		else
			throw Utils.throwException("TypeError", "unknown operation " + m + " of type '" + Utils.run("typename", a) + "'");
	}
	
	public static PythonObject doOperatorString(StringObject a, PythonObject b, String m) {
		boolean isString = b instanceof StringObject;
		switch (m){
			case __ADD__:
				return new StringObject(a.value + Utils.run("str", b).toString());
			case __MUL__:
				if (b instanceof IntObject) {
					// "a" * 5 -> "aaaaa"
					StringBuilder sb = new StringBuilder();
					for (int i=0; i<((IntObject)b).getJavaInt(); i++)
						sb.append(a.value);
					return new StringObject(sb.toString());
				}
				break; // Rest is not supported
			case __MOD__:
				throw Utils.throwException("TypeError", "string format not yet supported"); // :(
			case __LT__ :
				if (isString)
					return a.value.compareTo(((StringObject)b).value) < 0 ? BoolObject.TRUE : BoolObject.FALSE;
				break; // Unknown operation
			case __GT__ :
				if (isString)
					return a.value.compareTo(((StringObject)b).value) > 0 ? BoolObject.TRUE : BoolObject.FALSE;
				break; // Unknown operation
			case __LE__ :
				if (isString)
					return a.value.compareTo(((StringObject)b).value) <= 0 ? BoolObject.TRUE : BoolObject.FALSE;
				break; // Unknown operation
			case __GE__ :
				if (isString)
					return a.value.compareTo(((StringObject)b).value) >= 0 ? BoolObject.TRUE : BoolObject.FALSE;
				break; // Unknown operation
			case __EQ__ :
				if (isString)
					return ((StringObject)b).value.equals(a.value) ? BoolObject.TRUE : BoolObject.FALSE;
				return BoolObject.FALSE;
			case __NE__ :
				if (isString)
					return ((StringObject)b).value.equals(a.value) ? BoolObject.FALSE : BoolObject.TRUE;
				return BoolObject.TRUE;
		}
		
		if (b != null)
			throw Utils.throwException("TypeError", "unknown operation " + m + " of types '" + Utils.run("typename", a) + "' and '" + Utils.run("typename", b) + "'");
		else
			throw Utils.throwException("TypeError", "unknown operation " + m + " of type '" + Utils.run("typename", a) + "'");
	}
	
	private static PythonObject doOperatorBool(BoolObject a, PythonObject b, String m) {
		switch (m){
			case __NOT__:
				return BoolObject.fromBoolean(!a.truthValue());
			case __EQ__ :
				return b.truthValue() == a.truthValue() ? BoolObject.TRUE : BoolObject.FALSE;
			case __NE__ :
				return b.truthValue() != a.truthValue() ? BoolObject.TRUE : BoolObject.FALSE;
			default:
				// Rest is compared as int(a), b
				return doOperatorInt(IntObject.valueOf(a.getJavaInt()), b, m);
		}
	}
}
