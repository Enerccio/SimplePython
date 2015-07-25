package me.enerccio.sp.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.interpret.PythonExecutionException;
import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.parser.pythonLexer;
import me.enerccio.sp.parser.pythonParser;
import me.enerccio.sp.runtime.ModuleProvider;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.BoolObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.base.RealObject;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.callables.JavaFunctionObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.pointer.PointerObject;
import me.enerccio.sp.types.sequences.ListObject;
import me.enerccio.sp.types.sequences.SimpleIDAccessor;
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
	
	public static PythonObject cast(Object ret, Class<?> retType) {
		if (retType == Byte.class || retType == byte.class)
			return IntObject.valueOf(((Byte) ret).byteValue());
		if (retType == Integer.class || retType == int.class)
			return IntObject.valueOf(((Integer) ret).longValue());
		if (retType == Long.class || retType == long.class)
			return IntObject.valueOf(((Long) ret).longValue());
		if (retType == Float.class || retType == float.class)
			return new RealObject(((Float) ret).floatValue());
		if (retType == Double.class || retType == double.class)
			return new RealObject(((Double) ret).doubleValue());
		if (retType == String.class)
			return new StringObject((String) ret);
		if (retType == Void.class)
			return NoneObject.NONE;
		if (retType == Boolean.class || retType == boolean.class)
			return BoolObject.fromBoolean((Boolean) ret);
		if (ret instanceof PythonObject)
			return (PythonObject) ret;
		if (ret == null)
			return NoneObject.NONE;
		return PythonRuntime.runtime.getJavaClass(ret.getClass().getCanonicalName(), ret);
	}
	
	public static Object asJavaObject(Class<?> aType, PythonObject datum)
			throws PointerMethodIncompatibleException {
		if (aType == Integer.class || aType == int.class) {
			if (datum instanceof IntObject)
				return ((IntObject)datum).intValue();
			else
				return asJavaObject(aType, PythonInterpret.interpret.get().executeCall("int", datum));
		}

		if (aType == Long.class || aType == long.class) {
			if (datum instanceof IntObject)
				return ((IntObject)datum).longValue();
			else
				return asJavaObject(aType, PythonInterpret.interpret.get().executeCall("int", datum));
		}

		if (aType == Float.class || aType == float.class) {
			if (datum instanceof RealObject)
				return ((RealObject)datum).floatValue();
			else
				return asJavaObject(aType, PythonInterpret.interpret.get().executeCall("float", datum));
		}

		if (aType == Double.class || aType == double.class) {
			if (datum instanceof RealObject)
				return ((RealObject)datum).doubleValue();
			else
				return asJavaObject(aType, PythonInterpret.interpret.get().executeCall("float", datum));
		}

		if (aType == Boolean.class || aType == boolean.class) {
			return new Boolean(datum.truthValue());
		}

		if (aType == String.class) {
			if (datum instanceof StringObject)
				return ((StringObject) datum).getString();
		}

		if (datum instanceof PointerObject) {
			Class<?> ptype = ((PointerObject) datum).getObject().getClass();
			if (aType.isAssignableFrom(ptype))
				return ((PointerObject) datum).getObject();
		}
		
		if (aType.isAssignableFrom(datum.getClass()))
			return datum;

		throw new PointerMethodIncompatibleException();
	}

	public static PythonObject get(PythonObject container, String field) {
		return run("getattr", container, new StringObject(field));
	}
	
	public static PythonObject set(PythonObject container, String field, PythonObject value){
		return run("setattr", container, new StringObject(field), value);
	}

	public static PythonObject run(String function, PythonObject... args) {
		return PythonInterpret.interpret.get().executeCall(function, args);
	}

	public static RuntimeException throwException(String type, String text) {
		return new PythonExecutionException(run(type, new StringObject(text)));
	}
	
	public static RuntimeException throwException(String type) {
		return new PythonExecutionException(run(type));
	}

	public static void putPublic(PythonObject target, String key, PythonObject value) {
		target.fields.put(key, new AugumentedPythonObject(value, AccessRestrictions.PUBLIC));
	}

	public static <T> T peek(Stack<T> stack) {
		if (stack.empty())
			return null;
		T value = stack.peek();
		return value;
	}

	public static PythonObject staticMethodCall(boolean noTypeConversion, Class<?> clazz,
			String method, Class<?>... signature) {
		try {
			return new JavaFunctionObject(clazz.getMethod(method, signature), noTypeConversion);
		} catch (NoSuchMethodException e){
			// will not happen
			return null;
		}
	}
	
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
		int i = ((IntObject)idx).intValue();
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

	public static PythonObject getGlobal(String globalValue) {
		if (PythonInterpret.interpret.get().currentEnvironment.size() == 0)
			return PythonRuntime.runtime.generateGlobals().doGet(globalValue);
		return PythonInterpret.interpret.get().environment().get(new StringObject(globalValue), true, false);
	}

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

	public static void putPublic(Map<String, AugumentedPythonObject> sfields,
			String key, JavaMethodObject value) {
		sfields.put(key, new AugumentedPythonObject(value, AccessRestrictions.PUBLIC));
	}
}
