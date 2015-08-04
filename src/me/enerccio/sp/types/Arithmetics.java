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

	private static PythonObject doOperatorInt(IntObject a, PythonObject b,
			String m) {
		boolean isInt = b instanceof IntObject;
		boolean isFloat = b instanceof RealObject;
		boolean isComplex = b instanceof ComplexObject;
		boolean isNumber = b instanceof NumberObject;
		
		if (isInt || isFloat || isComplex){
			switch (m){
			case __ADD__:
				if (isInt){
					return IntObject.valueOf(a.intValue() + ((IntObject) b).intValue());
				}
				if (isFloat){
					return new RealObject((double)a.intValue() + ((RealObject) b).doubleValue());
				}
				if (isComplex){
					return new ComplexObject((double)a.intValue() + ((ComplexObject) b).getRealPart(), ((ComplexObject) b).getImagPart());
				}
			case __SUB__:
				if (isInt){
					return IntObject.valueOf(a.intValue() - ((IntObject) b).intValue());
				}
				if (isFloat){
					return new RealObject((double)a.intValue() - ((RealObject) b).doubleValue());
				}
				if (isComplex){
					return new ComplexObject((double)a.intValue() - ((ComplexObject) b).getRealPart(), ((ComplexObject) b).getImagPart());
				}
			case __MUL__:
				if (isInt){
					return IntObject.valueOf(a.intValue() * ((IntObject) b).intValue());
				}
				if (isFloat){
					return new RealObject((double)a.intValue() * ((RealObject) b).doubleValue());
				}
				if (isComplex){
					return new ComplexObject((double)a.intValue() * ((ComplexObject) b).getRealPart(), ((ComplexObject) b).getImagPart());
				}
			case __DIV__:
				if (isInt){
					return IntObject.valueOf(a.intValue() / ((IntObject) b).intValue());
				}
				if (isFloat){
					return new RealObject((double)a.intValue() / ((RealObject) b).doubleValue());
				}
				if (isComplex){
					return new ComplexObject((double)a.intValue() / ((ComplexObject) b).getRealPart(), ((ComplexObject) b).getImagPart());
				}
			case __MOD__:
				if (isInt){
					return IntObject.valueOf(a.intValue() % ((IntObject) b).intValue());
				}
				if (isFloat){
					return new RealObject((double)a.intValue() % ((RealObject) b).doubleValue());
				}
				if (isComplex){
					return new ComplexObject((double)a.intValue() % ((ComplexObject) b).getRealPart(), ((ComplexObject) b).getImagPart());
				}
			case __AND__:
				if (isInt){
					return IntObject.valueOf(a.intValue() & ((IntObject) b).intValue());
				}
				break; // Unknown operation
			case __OR__:
				if (isInt){
					return IntObject.valueOf(a.intValue() | ((IntObject) b).intValue());
				}
				break; // Unknown operation
			case __XOR__:
				if (isInt){
					return IntObject.valueOf(a.intValue() ^ ((IntObject) b).intValue());
				}
				break; // Unknown operation
			case __NOT__:
				if (isInt){
					return IntObject.valueOf(~a.intValue());
				}
				break; // Unknown operation
			case __POW__:
				if (isInt){
					return IntObject.valueOf((int) Math.pow(a.intValue(),  ((IntObject) b).intValue()));
				}
				if (isFloat){
					return new RealObject(Math.pow((double)a.intValue(), ((RealObject) b).doubleValue()));
				}
				if (isComplex){
					return new ComplexObject(Math.pow((double)a.intValue(), ((ComplexObject) b).getRealPart()), ((ComplexObject) b).getImagPart());
				}
			case __RSHIFT__:
				if (isInt){
					return IntObject.valueOf(a.intValue() >> ((IntObject) b).intValue());
				}
				break; // Unknown operation
			case __LSHIFT__:
				if (isInt){
					return IntObject.valueOf(a.intValue() << ((IntObject) b).intValue());
				}
				break; // Unknown operation
			case __LT__ :
				if (isNumber)
					return BoolObject.fromBoolean(a.getJavaInt().compareTo(((NumberObject)b).getJavaInt()) < 0);
				break; // Unknown operation
			case __GT__ :
				if (isNumber)
					return BoolObject.fromBoolean(a.getJavaInt().compareTo(((NumberObject)b).getJavaInt()) > 0);
				break; // Unknown operation
			case __LE__ :
				if (isNumber)
					return BoolObject.fromBoolean(a.getJavaInt().compareTo(((NumberObject)b).getJavaInt()) <= 0);
				break; // Unknown operation
			case __GE__ :
				if (isNumber)
					return BoolObject.fromBoolean(a.getJavaInt().compareTo(((NumberObject)b).getJavaInt()) >= 0);
				break; // Unknown operation
			case __EQ__ :
				if (isNumber)
					return BoolObject.fromBoolean(a.getJavaInt().compareTo(((NumberObject)b).getJavaInt()) == 0);
				break; // Unknown operation
			case __NE__ :
				if (isNumber)
					return BoolObject.fromBoolean(a.getJavaInt().compareTo(((NumberObject)b).getJavaInt()) != 0);
				break; // Unknown operation
			}
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
		
		if (isInt || isFloat || isComplex){
			switch (m){
			case __ADD__:
				if (isInt){
					return new RealObject(a.doubleValue() + ((IntObject) b).intValue());
				}
				if (isFloat){
					return new RealObject(a.doubleValue() + ((RealObject) b).doubleValue());
				}
				if (isComplex){
					return new ComplexObject(a.doubleValue() + ((ComplexObject) b).getRealPart(), ((ComplexObject) b).getImagPart());
				}
			case __SUB__:
				if (isInt){
					return new RealObject(a.doubleValue() - ((IntObject) b).intValue());
				}
				if (isFloat){
					return new RealObject(a.doubleValue() - ((RealObject) b).doubleValue());
				}
				if (isComplex){
					return new ComplexObject(a.doubleValue() - ((ComplexObject) b).getRealPart(), ((ComplexObject) b).getImagPart());
				}
			case __MUL__:
				if (isInt){
					return new RealObject(a.doubleValue() * ((IntObject) b).intValue());
				}
				if (isFloat){
					return new RealObject(a.doubleValue() * ((RealObject) b).doubleValue());
				}
				if (isComplex){
					return new ComplexObject(a.doubleValue() * ((ComplexObject) b).getRealPart(), ((ComplexObject) b).getImagPart());
				}
			case __DIV__:
				if (isInt){
					return new RealObject(a.doubleValue() / ((IntObject) b).intValue());
				}
				if (isFloat){
					return new RealObject(a.doubleValue() / ((RealObject) b).doubleValue());
				}
				if (isComplex){
					return new ComplexObject(a.doubleValue() / ((ComplexObject) b).getRealPart(), ((ComplexObject) b).getImagPart());
				}
			case __MOD__:
				if (isInt){
					return new RealObject(a.doubleValue() % ((IntObject) b).intValue());
				}
				if (isFloat){
					return new RealObject(a.doubleValue() % ((RealObject) b).doubleValue());
				}
				if (isComplex){
					return new ComplexObject(a.doubleValue() % ((ComplexObject) b).getRealPart(), ((ComplexObject) b).getImagPart());
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
					return new RealObject(Math.pow(a.doubleValue(), ((IntObject) b).intValue()));
				}
				if (isFloat){
					return new RealObject(Math.pow(a.doubleValue(), ((RealObject) b).doubleValue()));
				}
				if (isComplex){
					return new ComplexObject(Math.pow(a.doubleValue(), ((ComplexObject) b).getRealPart()), ((ComplexObject) b).getImagPart());
				}
			case __RSHIFT__:
				break; // Unknown operation
			case __LSHIFT__:
				break; // Unknown operation
			}
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
		
		if (isInt || isFloat || isComplex){
			switch (m){
			case __ADD__:
				if (isInt){
					return new ComplexObject(a.getRealPart() + ((IntObject)b).intValue(), a.getImagPart());
				}
				if (isFloat){
					return new ComplexObject(a.getRealPart() + ((RealObject)b).doubleValue(), a.getImagPart());
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
					return new ComplexObject(a.getRealPart() - ((RealObject)b).doubleValue(), a.getImagPart());
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
					return new ComplexObject(a.getRealPart() * ((RealObject)b).doubleValue(), a.getImagPart());
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
					return new ComplexObject(a.getRealPart() / ((RealObject)b).doubleValue(), a.getImagPart());
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
					return new ComplexObject(a.getRealPart() % ((RealObject)b).doubleValue(), a.getImagPart());
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
					return new ComplexObject(Math.pow(a.getRealPart(), ((RealObject)b).doubleValue()), a.getImagPart());
				}
				if (isComplex){
					return new ComplexObject(Math.pow(a.getRealPart(), ((ComplexObject)b).getRealPart()), 
							 Math.pow(a.getImagPart(), ((ComplexObject)b).getImagPart()));
				}
			case __RSHIFT__:
				break; // Unknown operation
			case __LSHIFT__:
				break; // Unknown operation
			}
		}
		
		if (b != null)
			throw Utils.throwException("TypeError", "unknown operation " + m + " of types '" + Utils.run("typename", a) + "' and '" + Utils.run("typename", b) + "'");
		else
			throw Utils.throwException("TypeError", "unknown operation " + m + " of type '" + Utils.run("typename", a) + "'");
	}
	
	private static PythonObject doOperatorString(StringObject a,
			PythonObject b, String m) {
		
		switch (m){
		case __ADD__:
			return new StringObject(a.value + Utils.run("str", b).toString());
		}
		
		if (b != null)
			throw Utils.throwException("TypeError", "unknown operation " + m + " of types '" + Utils.run("typename", a) + "' and '" + Utils.run("typename", b) + "'");
		else
			throw Utils.throwException("TypeError", "unknown operation " + m + " of type '" + Utils.run("typename", a) + "'");
	}
	
	private static PythonObject doOperatorBool(BoolObject a,
			PythonObject b, String m) {
		
		switch (m){
		case __NOT__:
			return BoolObject.fromBoolean(!a.truthValue());
		}
		
		if (b != null)
			throw Utils.throwException("TypeError", "unknown operation " + m + " of types '" + Utils.run("typename", a) + "' and '" + Utils.run("typename", b) + "'");
		else
			throw Utils.throwException("TypeError", "unknown operation " + m + " of type '" + Utils.run("typename", a) + "'");
	}
}
