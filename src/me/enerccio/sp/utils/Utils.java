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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Stack;
import java.util.TreeMap;

import me.enerccio.sp.compiler.Bytecode;
import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.interpret.CompiledBlockObject.DebugInformation;
import me.enerccio.sp.interpret.PythonExecutionException;
import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.parser.pythonLexer;
import me.enerccio.sp.parser.pythonParser;
import me.enerccio.sp.runtime.ModuleProvider;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.callables.JavaFunctionObject;
import me.enerccio.sp.types.sequences.ListObject;
import me.enerccio.sp.types.sequences.SimpleIDAccessor;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;

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
		target.fields.put(key, new AugumentedPythonObject(value, AccessRestrictions.PUBLIC));
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
			// will not happen
			return null;
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
	
	public static class ThrowingErrorListener extends BaseErrorListener {
		private String source;

		public ThrowingErrorListener(String loc) {
			this.source = loc;
		}

		@Override
		public void syntaxError(Recognizer<?, ?> recognizer,
				Object offendingSymbol, int line, int charPositionInLine,
				String msg, RecognitionException e)
				throws ParseCancellationException {
			throw new ParseCancellationException("file " + source + " line "
					+ line + ":" + charPositionInLine + " " + msg);
		}
	}

	/**
	 * Parses module provider into pythonParser
	 * @param provider
	 * @return
	 * @throws Exception
	 */
	public static pythonParser parse(ModuleProvider provider) throws Exception {
		ANTLRInputStream is = new ANTLRInputStream(provider.getSource());
		pythonLexer lexer = new pythonLexer(is);
		lexer.removeErrorListeners();
		lexer.addErrorListener(new ThrowingErrorListener(provider.getSrcFile()));
		CommonTokenStream stream = new CommonTokenStream(lexer);
		pythonParser parser = new pythonParser(stream);
		
		parser.removeErrorListeners();
		parser.addErrorListener(new ThrowingErrorListener(provider.getSrcFile()));
		return parser;
	}
	
	/**
	 * parses input stream into pythonParser
	 * @param provider
	 * @param srcFile
	 * @return
	 * @throws Exception
	 */
	public static pythonParser parse(InputStream provider, String srcFile) throws Exception {
		ANTLRInputStream is = new ANTLRInputStream(provider);
		pythonLexer lexer = new pythonLexer(is);
		lexer.removeErrorListeners();
		lexer.addErrorListener(new ThrowingErrorListener(srcFile));
		CommonTokenStream stream = new CommonTokenStream(lexer);
		pythonParser parser = new pythonParser(stream);
		
		parser.removeErrorListeners();
		parser.addErrorListener(new ThrowingErrorListener(srcFile));
		return parser;
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

	public static PythonObject doGet(SimpleIDAccessor o, PythonObject idx) {
		if (!(idx instanceof IntObject))
			throw throwException("TypeError", "Index must be int");
		int i = (int) ((IntObject)idx).intValue();
		if (i >= o.len() || i<-(o.len()))
			throw  throwException("IndexError", "Incorrect index, expected (" + -o.len() + ", " + o.len() + "), got " + i);
		return o.valueAt(morphAround(i, o.len()));
	}

	public static int morphAround(int i, int len) {
		if (i<0)
			return len-(Math.abs(i));
		return i;
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
	 * Resolves type hierarchy diamonds via L3. Returns linearized list of types. 
	 * @param clo
	 * @return
	 */
	public static List<ClassObject> resolveDiamonds(ClassObject clo) {
		List<ClassObject> ll = asListOfClasses(clo);
		List<ClassObject> linear = linearize(ll);
		Collections.reverse(linear);
		return linear;
	}

	private static List<ClassObject> linearize(List<ClassObject> ll) {
		List<ClassObject> merged = new ArrayList<ClassObject>();
		merged.add(ll.get(0));
		if (ll.size() == 1)
			return merged;
		List<List<ClassObject>> mergeList = new ArrayList<List<ClassObject>>();
		for (int i=1; i<ll.size(); i++)
			mergeList.add(linearize(asListOfClasses(ll.get(i))));
		for (ClassObject o : merge(mergeList, ll.subList(1, ll.size())))
			merged.add(o);
		return merged;
	}

	private static List<ClassObject> merge(List<List<ClassObject>> mergeList,
			List<ClassObject> subList) {
		mergeList.add(subList);
		
		List<ClassObject> m = new ArrayList<ClassObject>();
		while (true){
			List<ClassObject> suitable = null;
			
			outer:
			for (List<ClassObject> testee : mergeList){
				if (testee.size() == 0)
					continue;
				suitable = testee;
				ClassObject head = testee.get(0);
				for (List<ClassObject> tested : mergeList)
					if (testee != tested)
						if (tails(tested).contains(head)){
							suitable = null;
							continue outer;
						}
				if (testee != null)
					break;
			}
			if (suitable == null) {
				for (List<ClassObject> cllist : mergeList)
					if (cllist.size() != 0)
						throw Utils.throwException("TypeError", "unsuitable class hierarchy!");
				return m;
			}
			
			ClassObject head = suitable.get(0);
			m.add(head);
			for (List<ClassObject> cllist : mergeList)
				cllist.remove(head);
		}
	}

	private static List<ClassObject> tails(List<ClassObject> tested) {
		if (tested.size() == 0)
			return new ArrayList<ClassObject>();
		return tested.subList(1, tested.size());
	}

	private static List<ClassObject> asListOfClasses(ClassObject clo) {
		List<ClassObject> cl = new ArrayList<ClassObject>();
		cl.add(clo);
		for (PythonObject o : ((TupleObject) clo.fields.get(ClassObject.__BASES__).object).getObjects())
			cl.add((ClassObject) o);
		return cl;
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

	/**
	 * Converts InputStream into byte array
	 * @param input
	 * @return
	 * @throws IOException
	 */
	public static byte[] toByteArray(InputStream input) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
	    copy(input, output);
	    return output.toByteArray();
	}

	public static long copy(InputStream input, OutputStream output) throws IOException {
		return copy(input, output, new byte[4096]);
	}

	public static long copy(InputStream input, OutputStream output, byte[] buffer) throws IOException {
		long count = 0L;
	    int n = 0;
	    while (-1 != (n = input.read(buffer))) {
	      output.write(buffer, 0, n);
	      count += n;
	    }
	    return count;
	}

	private static ThreadLocal<Integer> kkey = new ThreadLocal<Integer>();
	public static byte[] compile(List<PythonBytecode> bytecode,
			Map<Integer, PythonObject> mmap, NavigableMap<Integer, DebugInformation> dmap) throws Exception {
		Map<PythonObject, Integer> rmap = new HashMap<PythonObject, Integer>();
		Map<Integer, Integer> rmapMap = new TreeMap<Integer, Integer>();
		Map<Integer, Integer> jumpMap = new TreeMap<Integer, Integer>();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream w = new DataOutputStream(baos);
		
		DebugInformation d = null;
		kkey.set(0);
		if (bytecode.get(bytecode.size() - 1).getOpcode() != Bytecode.NOP)
			bytecode.add(Bytecode.makeBytecode(Bytecode.NOP));
		
		int itc = 0;
		for (PythonBytecode b : bytecode){
			int ii = baos.size();
			rmapMap.put(itc, ii);
			
			if (d == null || notEqual(d, b)){
				d = new DebugInformation();
				d.charno = b.debugInLine;
				d.lineno = b.debugLine;
				d.modulename = b.debugModule == null ? "<nomodule>" : b.debugModule;
				dmap.put(ii, d);
			}
			
			w.writeByte(b.getOpcode().id);
			
			switch(b.getOpcode()){
			case ACCEPT_ITER:
				jumpMap.put(itc, b.intValue);
				w.writeInt(b.intValue);
				break;
			case CALL:
				w.writeInt(b.intValue);
				break;
			case DUP:
				w.writeInt(b.intValue);
				break;
			case YIELD:
				w.writeInt(insertValue(new StringObject(b.stringValue), mmap, rmap));
				w.writeInt(b.intValue);
				break;
			case ECALL:
				w.writeInt(b.intValue);
				break;
			case GET_ITER:
				jumpMap.put(itc, b.intValue);
				w.writeInt(0);
				break;
			case GETATTR:
				w.writeInt(insertValue(b.stringValue == null ? NoneObject.NONE : new StringObject(b.stringValue), mmap, rmap));
				break;
			case GOTO:
				jumpMap.put(itc, b.intValue);
				w.writeInt(0);
				break;
			case IMPORT:
				w.writeInt(insertValue(new StringObject(b.stringValue), mmap, rmap));
				w.writeInt(insertValue(new StringObject((String)b.object), mmap, rmap));
				break;
			case ISINSTANCE:
				break;
			case JUMPIFFALSE:
				jumpMap.put(itc, b.intValue);
				w.writeInt(0);
				break;
			case JUMPIFNONE:
				jumpMap.put(itc, b.intValue);
				w.writeInt(0);
				break;
			case JUMPIFNORETURN:
				jumpMap.put(itc, b.intValue);
				w.writeInt(0);
				break;
			case JUMPIFTRUE:
				jumpMap.put(itc, b.intValue);
				w.writeInt(0);
				break;
			case LOAD:
				w.writeInt(insertValue(new StringObject(b.stringValue), mmap, rmap));
				break;
			case LOADGLOBAL:
				w.writeInt(insertValue(new StringObject(b.stringValue), mmap, rmap));
				break;
			case LOADDYNAMIC:
				w.writeInt(insertValue(new StringObject(b.stringValue), mmap, rmap));
				break;
			case NOP:
				break;
			case POP:
				break;
			case OPEN_LOCALS:
				break;
			case PUSH_LOCALS:
				break;
			case PUSH:
				w.writeInt(insertValue(b.value, mmap, rmap));
				break;
			case KWARG:
				String[] ss = (String[]) b.object;
				w.writeInt(ss.length);
				for (String s : ss)
					w.writeInt(insertValue(new StringObject(s), mmap, rmap));
				break;
			case PUSH_DICT:
				w.writeInt(insertValue(b.mapValue, mmap, rmap));
				break;
			case PUSH_ENVIRONMENT:
				break;
			case PUSH_EXCEPTION:
				break;
			case PUSH_FRAME:
				jumpMap.put(itc, b.intValue);
				w.writeInt(b.intValue);
				break;
			case PUSH_LOCAL_CONTEXT:
				break;
			case RAISE:
				break;
			case RCALL:
				w.writeInt(b.intValue);
				break;
			case RERAISE:
				break;
			case RESOLVE_ARGS:
				break;
			case RETURN:
				w.writeInt(b.intValue);
				break;
			case SAVE:
				w.writeInt(insertValue(new StringObject(b.stringValue), mmap, rmap));
				break;
			case SAVEGLOBAL:
				w.writeInt(insertValue(new StringObject(b.stringValue), mmap, rmap));
				break;
			case SAVEDYNAMIC:
				w.writeInt(insertValue(new StringObject(b.stringValue), mmap, rmap));
				break;
			case SAVE_LOCAL:
				w.writeInt(insertValue(new StringObject(b.stringValue), mmap, rmap));
				break;
			case SETATTR:
				w.writeInt(insertValue(b.stringValue == null ? NoneObject.NONE : new StringObject(b.stringValue), mmap, rmap));
				break;
			case SETUP_LOOP:
				jumpMap.put(itc, b.intValue);
				w.writeInt(0);
				break;
			case SWAP_STACK:
				break;
			case TRUTH_VALUE:
				w.writeInt(b.intValue);
				break;
			case UNPACK_SEQUENCE:
				w.writeInt(b.intValue);
				break;
			}
			
			++itc;
		}
		
		byte[] data = baos.toByteArray();
		ByteBuffer b = ByteBuffer.wrap(data);
		
		for (Integer ppos : jumpMap.keySet()){
			Integer jumpval = jumpMap.get(ppos);
			Integer location = rmapMap.get(jumpval);
			Integer wloc = rmapMap.get(ppos) + 1;
			b.position(wloc);
			b.putInt(location);
		}
		
		return data;
	}

	private static boolean notEqual(DebugInformation d, PythonBytecode b) {
		return d.charno != b.debugInLine || d.lineno != b.debugLine || 
				(b.debugModule == null ? "<nomodule>".equals(d.modulename) : !d.modulename.equals(b.debugModule));
	}

	private static int insertValue(PythonObject v, Map<Integer, PythonObject> mmap, Map<PythonObject, Integer> rmap) {
		if (rmap.containsKey(v))
			return rmap.get(v);
		int key = kkey.get();
		kkey.set(key+1);
		rmap.put(v, key);
		mmap.put(key, v);
		return key;
	}

	public static String asString(byte[] compiled) {
		StringBuilder bd = new StringBuilder();
		for (byte b : compiled)
			bd.append("\\x" + Integer.toHexString(b & 0xFF));
		return bd.toString();
	}
}
