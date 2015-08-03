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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import me.enerccio.sp.interpret.PythonExecutionException;
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.CastFailedException;
import me.enerccio.sp.utils.PointerMethodIncompatibleException;
import me.enerccio.sp.utils.Utils;

/**
 * Java method or function callable. If called is null, it acts as static method callable.
 * @author Enerccio
 *
 */
public class JavaMethodObject extends CallableObject {
	private static final String __DOC__ = "__doc__";
	private static final long serialVersionUID = 23L;
	
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface ArgNames {
		String[] value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	public static @interface SpyDoc {
		String value();
	}

	/** Used iternalyy */
	private JavaMethodObject(Object caller, Method m, String[] argNames, String pydoc, boolean noTypeConversion){
		this.caller = caller;
		this.boundHandle = m;
		this.boundHandle.setAccessible(true);
		this.argNames = argNames;
		this.noTypeConversion = noTypeConversion;
		if (pydoc != null)
			this.fields.put(__DOC__, new AugumentedPythonObject(new StringObject(pydoc), null));
	}
	
	public JavaMethodObject(Object caller, Method m) {
		this.caller = caller;
		this.boundHandle = m;
		this.boundHandle.setAccessible(true);
		this.argNames = m.isAnnotationPresent(ArgNames.class) ? m.getAnnotation(ArgNames.class).value() : null;
		this.noTypeConversion = false;
		if (m.isAnnotationPresent(SpyDoc.class))
			this.fields.put(__DOC__, new AugumentedPythonObject(new StringObject(m.getAnnotation(SpyDoc.class).value()), null));
	}
	
	/**
	 * Creates new Java Method Object for static method that uses specified signature, with type conversion.
	 * 
	 * @param caller object this method is bound to
	 * @param name method name
	 */
	public JavaMethodObject(Class<?> cls, String name, Class<?>... tp) throws NoSuchMethodException, SecurityException {
		this(null,
				cls.getMethod(name, tp),
				cls.getMethod(name, tp).isAnnotationPresent(ArgNames.class)
					? cls.getMethod(name, tp).getAnnotation(ArgNames.class).value()
					: null,
				getPydoc(cls.getMethod(name, tp)),
				false);
	}

	
	/**
	 * Creates new Java Method Object for method that uses specified signature, with type conversion.
	 * 
	 * @param caller object this method is bound to
	 * @param name method name
	 */
	public JavaMethodObject(Object caller, String name, Class<?>... tp) throws NoSuchMethodException, SecurityException {
		this(caller,
				caller.getClass().getMethod(name, tp),
				caller.getClass().getMethod(name, tp).isAnnotationPresent(ArgNames.class)
					? caller.getClass().getMethod(name, tp).getAnnotation(ArgNames.class).value()
					: null,
				getPydoc(caller.getClass().getMethod(name, tp)),
				false);
	}

	/**
	 * Creates new Java Method Object for method that uses default signature - method(TupleObject, KwArgs),
	 * without type conversion.
	 * This constructor ignores @ArgNames annotation. 
	 * 
	 * @param caller object this method is bound to
	 * @param name method name
	 */
	public JavaMethodObject(Object caller, String name) throws NoSuchMethodException, SecurityException {
		this(caller,
				caller.getClass().getMethod(name, new Class<?>[]{TupleObject.class, KwArgs.class}),
				null,
				getPydoc(caller.getClass().getMethod(name, new Class<?>[]{TupleObject.class, KwArgs.class})),
				true);
	}

	/**
	 * Creates new Java Method Object for method that uses default signature - method(TupleObject, KwArgs),
	 * without type conversion.
	 * This constructor ignores @ArgNames annotation. 
	 * 
	 * @param caller object this method is bound to
	 * @param name method name
	 */
	public JavaMethodObject(Class<?> cls, String name) throws NoSuchMethodException, SecurityException {
		this(null,
				cls.getMethod(name, new Class<?>[]{TupleObject.class, KwArgs.class}),
				null,
				getPydoc(cls.getMethod(name, new Class<?>[]{TupleObject.class, KwArgs.class})),
				true);
	}
	
	public static JavaMethodObject noArgMethod(Class<?> cls, String name) throws NoSuchMethodException, SecurityException {
		return new JavaMethodObject(cls, name, new Class<?>[] {} );
	}
	
	public static JavaMethodObject noArgMethod(Object o, String name) throws NoSuchMethodException, SecurityException {
		return new JavaMethodObject(o, name, new Class<?>[] {} );
	}
	
	protected JavaMethodObject(Method m, boolean noTypeConversion) {
		this.caller = null;
		this.boundHandle = m;
		this.boundHandle.setAccessible(true);
		this.argNames = null;
		this.noTypeConversion = noTypeConversion;
	}

	private static String getPydoc(Method m) {
		return m.isAnnotationPresent(SpyDoc.class) ?  m.getAnnotation(SpyDoc.class).value() : null;
	}

	/** Method handle */
	protected final Method boundHandle;
	/** Object bound to the method or null if static */
	private final Object caller;
	/** Whether to do type conversion or not */
	private final boolean noTypeConversion;
	/** List of argument names, if set */
	private final String[] argNames; 
	
	@Override
	public PythonObject call(TupleObject args, KwArgs kwargs) {
		try {
			return doCall(args, kwargs);
		} catch (PointerMethodIncompatibleException e) {
			throw Utils.throwException("TypeError", e.getMessage(), e);
		}
	}
	
	/**
	 * Executes java method. 
	 * @param args arguments to use
	 * @return
	 */
	public PythonObject doCall(TupleObject args, KwArgs kwargs) throws PointerMethodIncompatibleException {
		try {
			if (noTypeConversion){
				return Utils.cast(invoke(args, kwargs), boundHandle.getReturnType());
			}
		} catch (PythonExecutionException e){
			throw e;
		} catch (InvocationTargetException e){
			if (e.getTargetException() instanceof PythonExecutionException)
				throw (RuntimeException)e.getTargetException();
			throw Utils.throwException("TypeError", toString() + ": failed java call", e);
		} catch (Throwable e){
			throw Utils.throwException("TypeError", toString() + ": failed java call", e);
		}
		
		// Prepare stuff
		Class<?>[] argTypes = boundHandle.getParameterTypes();
		Object[] jargs = new Object[argTypes.length];
		int i = 0;
		
		try {
			// Fill arguments from tuple
			while (i < args.len()) {
				if ((i >= jargs.length) || (argTypes[i] == KwArgs.class))
					throw new PointerMethodIncompatibleException(toString() + " doesn't take " + args.len() + " arguments");
				jargs[i] = Utils.asJavaObject(argTypes[i], args.get(i));
				if ((kwargs != null) && (argNames != null) && kwargs.contains(argNames[i]))
					throw new PointerMethodIncompatibleException(toString() + " got multiple values for keyword argument '" + argNames[i] + "'");
				++i;
			}
			// Fill arguments from kwargs, if any
			if ((argNames != null) && (kwargs != null)) {
				while (i < argTypes.length) {
					if (argTypes[i] == KwArgs.class)
						// Last one
						break;
					if (kwargs.contains(argNames[i])) {
						jargs[i] = Utils.asJavaObject(argTypes[i], kwargs.consume(argNames[i]));
					} else {
						throw new PointerMethodIncompatibleException(toString() + " values for argument '" + argNames[i] + "' missing");
					}
					i++;
				}
			}
		} catch (CastFailedException e){
			if ((argNames == null) || (i >= argNames.length))
				throw new PointerMethodIncompatibleException(toString() + " cannot convert value for argument " + i);
			else
				throw new PointerMethodIncompatibleException(toString() + " cannot convert value for argument '" + argNames[i] + "'");
		}
	
		if (i < argTypes.length) {
			if ((kwargs != null) && (argTypes[i] != KwArgs.class)) {
				try {
					kwargs.checkEmpty(toString());
				} catch (PythonExecutionException e) {
					throw new PointerMethodIncompatibleException(e.getMessage());
				}
			} else if (argTypes[i] == KwArgs.class) {
				jargs[i] = kwargs == null ? KwArgs.EMPTY : kwargs;
			}
		}
		
		try {
			return Utils.cast(invoke(jargs), boundHandle.getReturnType());
		} catch (PythonExecutionException e){
			throw e;
		} catch (InvocationTargetException e){
			if (e.getTargetException() instanceof PythonExecutionException)
				throw (RuntimeException)e.getTargetException();
			throw Utils.throwException("TypeError", toString() + ": failed java call", e);
		} catch (Throwable e) {
			throw Utils.throwException("TypeError", toString() + ": failed java call", e);
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
		return "<java method " + boundHandle.getName() + " of object " + caller.getClass().getSimpleName()  + ">";
	}

	/**
	 * Returns clone of itself with bound object rebinded
	 * @param self
	 * @return
	 */
	public JavaMethodObject cloneWithThis(Object self) {
		return new JavaMethodObject(self, boundHandle, argNames,
				this.fields.containsKey(__DOC__) ? this.fields.get(__DOC__).object.toString() : null, 
				noTypeConversion);
	}
}
