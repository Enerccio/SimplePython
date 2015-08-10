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
package me.enerccio.sp.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.interpret.PythonExecutionException;
import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.JavaFunctionObject;
import me.enerccio.sp.types.sequences.ListObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;

public class Utils {

	
	/**
	 * Adds object to the left of the array, ie x + [a, b] => [x, a, b]
	 * 
	 * @param data
	 *            what to be pushed
	 * @param array
	 *            array to be pushed in
	 * @return new array with object pushed in left
	 */
	public static <T> T[] pushLeft(T data, T[] array) {
		@SuppressWarnings("unchecked")
		T[] pushed = (T[]) Array.newInstance(array.getClass()
				.getComponentType(), array.length + 1);
		pushed[0] = data;
		System.arraycopy(array, 0, pushed, 1, array.length);
		return pushed;
	}

	public static PythonObject get(PythonObject container, String field) {
		return run("getattr", container, new StringObject(field));
	}
	
	public static PythonObject set(PythonObject container, String field, PythonObject value){
		return run("setattr", container, new StringObject(field), value);
	}

	/** Executes builtin with specified parameters and waits until it finishes */ 
	public static PythonObject run(String function, PythonObject... args) {
		return PythonInterpreter.interpreter.get().executeCall(true, function, args);
	}
	
	/**
	 * throws exception of that type, that text and that cause
	 * @param type
	 * @param text
	 * @return
	 */
	public static RuntimeException throwException(String type, String text, Throwable cause) {
		return new PythonExecutionException(run(type, new StringObject(text)), cause);
	}
	
	/**
	 * throws exception of that type and that text
	 * @param type
	 * @param text
	 * @return
	 */
	public static RuntimeException throwException(String type, String text) {
		return new PythonExecutionException(run(type, new StringObject(text)));
	}
	
	/**
	 * throws exception of that type
	 * @param type
	 * @return
	 */
	public static RuntimeException throwException(String type) {
		return new PythonExecutionException(run(type));
	}

	/**
	 * puts value into field of this object publicly
	 * @param target object
	 * @param key name of the field
	 * @param value public or private
	 */
	public static void putPublic(PythonObject target, String key, PythonObject value) {
		target.getEditableFields().put(key, new AugumentedPythonObject(value, AccessRestrictions.PUBLIC));
	}

	/**
	 * returns top of the stack or null if empty
	 * @param stack
	 * @return
	 */
	public static <T> T peek(Stack<T> stack) {
		if (stack.empty())
			return null;
		T value = stack.peek();
		return value;
	}

	/**
	 * Wraps the method into python object
	 * @param noTypeConversion whether or not to do type conversion
	 * @param clazz class of which method to wrap
	 * @param method method name
	 * @param signature method signature
	 * @return wrapped method
	 */
	public static PythonObject staticMethodCall(boolean noTypeConversion, Class<?> clazz,
			String method, Class<?>... signature) {
		try {
			return new JavaFunctionObject(clazz.getDeclaredMethod(method, signature), noTypeConversion);
		} catch (NoSuchMethodException e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Wraps the method into python object
	 * @param clazz class of which method to wrap
	 * @param method method name
	 * @param signature method signature
	 * @return wrapped method
	 */
	public static PythonObject staticMethodCall(Class<?> clazz,
			String method, Class<?>... signature) {
		return staticMethodCall(false, clazz, method, signature);
	}

	@SuppressWarnings("unchecked")
	public static Collection<? extends PythonBytecode> asList(
			PythonObject pythonObject) {
		ArrayList<PythonObject> pa = new ArrayList<PythonObject>();
		if (pythonObject instanceof TupleObject)
			pa.addAll(Arrays.asList(((TupleObject) pythonObject).getObjects()));
		else
			pa.addAll(((ListObject)pythonObject).objects);
		return (ArrayList<PythonBytecode>)(Object)pa;
	}

	public static boolean equals(PythonObject a, PythonObject b) {
		return a.equals(b);
	}

	public static PythonObject list2tuple(List<? extends PythonObject> list) {
		return new TupleObject(list.toArray(new PythonObject[list.size()]));
	}

	public static <T> List<T> reverse(List<T> l) {
		List<T> tlist = new ArrayList<T>(l);
		Collections.reverse(tlist);
		return tlist;
	}

	public static <T> T[] removeFirst(T[] objects) {
		if (objects.length == 0)
			return null;
		@SuppressWarnings("unchecked")
		T[] copy = (T[]) Array.newInstance(objects.getClass()
				.getComponentType(), objects.length - 1);
		for (int i=1; i<objects.length; i++)
			copy[i-1] = objects[i];
		return copy;
	}

	/**
	 * Returns global variable
	 * @param variable
	 * @return
	 */
	public static PythonObject getGlobal(String variable) {
		if (PythonInterpreter.interpreter.get().currentFrame.size() == 0)
			return PythonRuntime.runtime.getGlobals().doGet(variable);
		return PythonInterpreter.interpreter.get().environment().get(new StringObject(variable), true, false);
	}

	public static String asString(byte[] compiled) {
		StringBuilder bd = new StringBuilder();
		for (byte b : compiled)
			bd.append("\\x" + Integer.toHexString(b & 0xFF));
		return bd.toString();
	}
}
