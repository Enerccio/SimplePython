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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.Arithmetics;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public abstract class NumberObject extends PythonObject {
	private static final long serialVersionUID = 8168239961379175666L;
	public static final String __INT__ = "__int__";

	public NumberObject(){
		
	}
	
	@Override
	protected void registerObject(){
		
	}
	
	private static Map<String, AugumentedPythonObject> sfields = Collections.synchronizedMap(new HashMap<String, AugumentedPythonObject>());
	
	static {
		try {
			sfields.put(__INT__, new AugumentedPythonObject(
					new JavaMethodObject(null, NumberObject.class.getMethod("intValue", 
							new Class<?>[]{TupleObject.class}), true), AccessRestrictions.PUBLIC));
			sfields.put(Arithmetics.__ADD__, new AugumentedPythonObject(
					new JavaMethodObject(null, NumberObject.class.getMethod("add", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			sfields.put(Arithmetics.__SUB__, new AugumentedPythonObject(
					new JavaMethodObject(null, NumberObject.class.getMethod("sub", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			sfields.put(Arithmetics.__MUL__, new AugumentedPythonObject(
					new JavaMethodObject(null, NumberObject.class.getMethod("mul", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			sfields.put(Arithmetics.__DIV__, new AugumentedPythonObject(
					new JavaMethodObject(null, NumberObject.class.getMethod("div", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			sfields.put(Arithmetics.__MOD__, new AugumentedPythonObject(
					new JavaMethodObject(null, NumberObject.class.getMethod("mod", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			sfields.put(Arithmetics.__AND__, new AugumentedPythonObject(
					new JavaMethodObject(null, NumberObject.class.getMethod("and", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			sfields.put(Arithmetics.__OR__, new AugumentedPythonObject(
					new JavaMethodObject(null, NumberObject.class.getMethod("or", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			sfields.put(Arithmetics.__NOT__, new AugumentedPythonObject(
					new JavaMethodObject(null, NumberObject.class.getMethod("not", 
							new Class<?>[]{}), false), AccessRestrictions.PUBLIC));
			sfields.put(Arithmetics.__XOR__, new AugumentedPythonObject(
					new JavaMethodObject(null, NumberObject.class.getMethod("xor", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			sfields.put(Arithmetics.__POW__, new AugumentedPythonObject(
					new JavaMethodObject(null, NumberObject.class.getMethod("pow", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			sfields.put(Arithmetics.__RSHIFT__, new AugumentedPythonObject(
					new JavaMethodObject(null, NumberObject.class.getMethod("rs", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			sfields.put(Arithmetics.__LSHIFT__, new AugumentedPythonObject(
					new JavaMethodObject(null, NumberObject.class.getMethod("ls", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			
			
			sfields.put(Arithmetics.__LT__, new AugumentedPythonObject(
					new JavaMethodObject(null, NumberObject.class.getMethod("lt", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			sfields.put(Arithmetics.__LE__, new AugumentedPythonObject(
					new JavaMethodObject(null, NumberObject.class.getMethod("le", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			sfields.put(Arithmetics.__EQ__, new AugumentedPythonObject(
					new JavaMethodObject(null, NumberObject.class.getMethod("eq", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			sfields.put(Arithmetics.__NE__, new AugumentedPythonObject(
					new JavaMethodObject(null, NumberObject.class.getMethod("ne", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			sfields.put(Arithmetics.__GE__, new AugumentedPythonObject(
					new JavaMethodObject(null, NumberObject.class.getMethod("ge", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			sfields.put(Arithmetics.__GT__, new AugumentedPythonObject(
					new JavaMethodObject(null, NumberObject.class.getMethod("gt", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));

		} catch (Exception e) {
			
		}
	}

	@Override
	public void newObject() {	
		super.newObject();
		try {
			String m;
			
			m = __INT__;
			fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
					AccessRestrictions.PUBLIC));
			
			m = Arithmetics.__ADD__;
			fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
					AccessRestrictions.PUBLIC));
			m = Arithmetics.__SUB__;
			fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
					AccessRestrictions.PUBLIC));
			m = Arithmetics.__MUL__;
			fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
					AccessRestrictions.PUBLIC));
			m = Arithmetics.__DIV__;
			fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
					AccessRestrictions.PUBLIC));
			m = Arithmetics.__MOD__;
			fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
					AccessRestrictions.PUBLIC));
			m = Arithmetics.__AND__;
			fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
					AccessRestrictions.PUBLIC));
			m = Arithmetics.__OR__;
			fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
					AccessRestrictions.PUBLIC));
			m = Arithmetics.__NOT__;
			fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
					AccessRestrictions.PUBLIC));
			m = Arithmetics.__XOR__;
			fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
					AccessRestrictions.PUBLIC));
			m = Arithmetics.__POW__;
			fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
					AccessRestrictions.PUBLIC));
			m = Arithmetics.__RSHIFT__;
			fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
					AccessRestrictions.PUBLIC));
			m = Arithmetics.__LSHIFT__;
			fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
					AccessRestrictions.PUBLIC));
			m = Arithmetics.__LT__;
			fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
					AccessRestrictions.PUBLIC));
			m = Arithmetics.__LE__;
			fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
					AccessRestrictions.PUBLIC));
			m = Arithmetics.__EQ__;
			fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
					AccessRestrictions.PUBLIC));
			m = Arithmetics.__NE__;
			fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
					AccessRestrictions.PUBLIC));
			m = Arithmetics.__GE__;
			fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
					AccessRestrictions.PUBLIC));
			m = Arithmetics.__GT__;
			fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
					AccessRestrictions.PUBLIC));

		} catch (Exception e) {
			
		}
	};
	
	protected abstract PythonObject getIntValue();
	
	/** Converts number to BigInteger */ 
	public abstract BigInteger getJavaInt();
	/** Converts number to float */
	public abstract double getJavaFloat();
	
	public PythonObject intValue(TupleObject args){
		if (args.len() != 0)
			throw Utils.throwException("TypeError", "__int__ requires zero parameters");
		return getIntValue();
	}
	
	@Override
	public PythonObject set(String key, PythonObject localContext,
			PythonObject value) {
		if (!fields.containsKey(key))
			throw Utils.throwException("AttributeError", "'" + 
					Utils.run("str", Utils.run("type", this)) + "' object has no attribute '" + key + "'");
		throw Utils.throwException("AttributeError", "'" + 
				Utils.run("str", Utils.run("type", this)) + "' object attribute '" + key + "' is read only");
	}

	@Override
	public void create(String key, AccessRestrictions restrictions) {
		
	}
	
	public PythonObject add(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__ADD__);
	}
	
	public PythonObject sub(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__SUB__);
	}
	
	public PythonObject mul(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__MUL__);
	}
	
	public PythonObject div(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__DIV__);
	}
	
	public PythonObject mod(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__MOD__);
	}
	
	public PythonObject and(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__AND__);
	}
	
	public PythonObject or(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__OR__);
	}
	
	public PythonObject xor(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__XOR__);
	}
	
	public PythonObject not(){
		return Arithmetics.doOperator(this, null, Arithmetics.__NOT__);
	}
	
	public PythonObject pow(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__POW__);
	}
	
	public PythonObject ls(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__LSHIFT__);
	}
	
	public PythonObject rs(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__RSHIFT__);
	}
	
	public PythonObject lt(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__LT__);
	}
	
	public PythonObject le(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__LE__);
	}
	
	public PythonObject eq(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__EQ__);
	}
	
	public PythonObject ne(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__NE__);
	}
	
	public PythonObject gt(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__GT__);
	}
	
	public PythonObject ge(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__GE__);
	}
}
