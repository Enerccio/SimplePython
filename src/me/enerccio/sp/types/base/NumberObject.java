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

import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.Arithmetics;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

/**
 * Root number object. Represents all numbers
 * @author Enerccio
 *
 */
public abstract class NumberObject extends PythonObject {
	private static final long serialVersionUID = 8168239961379175666L;
	public static final String __INT__ = "__int__";

	public NumberObject(){
		
	}
	
	@Override
	protected void registerObject(){
		
	}
	
	private static Map<String, JavaMethodObject> sfields = new HashMap<String, JavaMethodObject>();
	
	static {
		try {
			sfields.put(__INT__, 			new JavaMethodObject(NumberObject.class, "intValue"));
			sfields.put(Arithmetics.__ADD__, new JavaMethodObject(NumberObject.class, "add", PythonObject.class));
			sfields.put(Arithmetics.__SUB__, new JavaMethodObject(NumberObject.class, "sub", PythonObject.class));
			sfields.put(Arithmetics.__MUL__, new JavaMethodObject(NumberObject.class, "mul", PythonObject.class));
			sfields.put(Arithmetics.__DIV__, new JavaMethodObject(NumberObject.class, "div", PythonObject.class));
			sfields.put(Arithmetics.__MOD__, new JavaMethodObject(NumberObject.class, "mod", PythonObject.class));
			sfields.put(Arithmetics.__AND__, new JavaMethodObject(NumberObject.class, "and", PythonObject.class));
			sfields.put(Arithmetics.__OR__, new JavaMethodObject(NumberObject.class, "or", PythonObject.class));
			sfields.put(Arithmetics.__XOR__, new JavaMethodObject(NumberObject.class, "xor", PythonObject.class));
			sfields.put(Arithmetics.__POW__, new JavaMethodObject(NumberObject.class, "pow", PythonObject.class));
			sfields.put(Arithmetics.__RSHIFT__, new JavaMethodObject(NumberObject.class, "rs", PythonObject.class));
			sfields.put(Arithmetics.__LSHIFT__, new JavaMethodObject(NumberObject.class, "ls", PythonObject.class));
			sfields.put(Arithmetics.__LT__, new JavaMethodObject(NumberObject.class, "lt", PythonObject.class));
			sfields.put(Arithmetics.__LE__, new JavaMethodObject(NumberObject.class, "le", PythonObject.class));
			sfields.put(Arithmetics.__EQ__, new JavaMethodObject(NumberObject.class, "eq", PythonObject.class));
			sfields.put(Arithmetics.__NE__, new JavaMethodObject(NumberObject.class, "ne", PythonObject.class));
			sfields.put(Arithmetics.__GE__, new JavaMethodObject(NumberObject.class, "ge", PythonObject.class));
			sfields.put(Arithmetics.__GT__, new JavaMethodObject(NumberObject.class, "gt", PythonObject.class));

		} catch (Exception e) {
			throw new RuntimeException("Fuck", e);
		}
	}

	@Override
	public void newObject() {	
		super.newObject();
		bindMethods(sfields);
	};
	
	protected abstract PythonObject getIntValue();
	
	/** Converts number to BigInteger */ 
	public abstract Long getJavaInt();
	/** Converts number to float */
	public abstract double getJavaFloat();
	
	/**
	 * Returns int value
	 * @param args
	 * @return
	 */
	public PythonObject intValue(TupleObject args, KwArgs kw){
		args.notExpectingArgs(kw);
		return getIntValue();
	}
	
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
