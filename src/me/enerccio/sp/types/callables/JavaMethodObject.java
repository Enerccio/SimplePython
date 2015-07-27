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
package me.enerccio.sp.types.callables;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import me.enerccio.sp.interpret.PythonExecutionException;
import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.PointerMethodIncompatibleException;
import me.enerccio.sp.utils.Utils;

public class JavaMethodObject extends CallableObject {
	private static final long serialVersionUID = 23L;

	public JavaMethodObject(Object caller, Method m, boolean noTypeConversion){
		this.caller = caller;
		this.boundHandle = m;
		m.setAccessible(true);
		this.noTypeConversion = noTypeConversion;
	}
	
	private JavaMethodObject(){
		
	}
	
	protected Method boundHandle;
	private Object caller;
	private boolean noTypeConversion;
	
	@Override
	public PythonObject call(TupleObject args) {
		try {
			if (noTypeConversion){
				return Utils.cast(invoke(args), boundHandle.getReturnType());
			}
		} catch (PythonExecutionException e){
			throw e;
		} catch (InvocationTargetException e){
			if (e.getTargetException() instanceof PythonExecutionException)
				throw (RuntimeException)e.getTargetException();
			throw Utils.throwException("TypeError", toString() + ": failed java call");
		} catch (Throwable e){
			throw Utils.throwException("TypeError", toString() + ": failed java call");
		}
		
		Object[] jargs = new Object[args.len()];
		Class<?>[] types = boundHandle.getParameterTypes();
		
		if (types.length != jargs.length){
			throw Utils.throwException("TypeError", toString() + ": wrong number of parameters, expected " + types.length + ", got " + jargs.length);
		}
		
		int i=0;
		for (PythonObject o : args.getObjects()){
			try {
				jargs[i] = Utils.asJavaObject(types[i], o);
				++i;
			} catch (PointerMethodIncompatibleException e){
				throw Utils.throwException("TypeError", toString() + ": cannot convert python objects to java objects for arguments of this method");
			}
		}
		
		try {
			return Utils.cast(invoke(jargs), boundHandle.getReturnType());
		} catch (PythonExecutionException e){
			throw e;
		} catch (InvocationTargetException e){
			if (e.getTargetException() instanceof PythonExecutionException)
				throw (RuntimeException)e.getTargetException();
			throw Utils.throwException("TypeError", toString() + ": failed java call");
		} catch (Throwable e) {
			throw Utils.throwException("TypeError", toString() + ": failed java call");
		}
	}

	private Object invoke(Object... args) throws Throwable {
		return boundHandle.invoke(caller, args);
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
	public void create(String key, AccessRestrictions restrictions, PythonObject localContext) {
		
	}

	@Override
	protected String doToString() {
		if (caller == null)
			return "<java function " + boundHandle.getName() + ">";
		return "<java method " + boundHandle.toString() + " of object " + caller.getClass().getName()  + ">";
	}

	public JavaMethodObject cloneWithThis(Object self) {
		JavaMethodObject m = new JavaMethodObject();
		m.boundHandle = boundHandle;
		m.caller = self;
		m.noTypeConversion = noTypeConversion;
		return m;
	}
}
