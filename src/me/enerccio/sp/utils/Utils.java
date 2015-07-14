package me.enerccio.sp.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import me.enerccio.sp.compiler.Bytecode;
import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.interpret.PythonExecutionException;
import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.parser.pythonLexer;
import me.enerccio.sp.parser.pythonParser;
import me.enerccio.sp.runtime.ModuleProvider;
import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.BoolObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.base.RealObject;
import me.enerccio.sp.types.callables.JavaFunctionObject;
import me.enerccio.sp.types.callables.UserMethodObject;
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
			return new IntObject(((Byte) ret).byteValue());
		if (retType == Integer.class || retType == int.class)
			return new IntObject(((Integer) ret).longValue());
		if (retType == Long.class || retType == long.class)
			return new IntObject(((Long) ret).longValue());
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
		return new PointerObject(ret);
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
		return stack.peek();
	}

	public static PythonObject staticMethodCall(Class<?> clazz,
			String method, Class<?>... signature) {
		try {
			return new JavaFunctionObject(clazz.getMethod(method, signature), false);
		} catch (NoSuchMethodException e){
			// will not happen
			return null;
		}
	}
	
	private static class ThrowingErrorListener extends BaseErrorListener {
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
		if (i >= o.len() || i<0)
			throw  throwException("IndexError", "Incorrect index, expected <0, " + o.len() + "), got " + i);
		return o.valueAt(i);
	}

	public static List<PythonBytecode> methodCall(UserMethodObject o, TupleObject args) {
		PythonBytecode b = null;
		List<PythonBytecode> l = new ArrayList<PythonBytecode>();

		// []
		l.add(Bytecode.makeBytecode(Bytecode.PUSH_ENVIRONMENT));
		l.add(b = Bytecode.makeBytecode(Bytecode.PUSH));
		b.value = o.get(UserMethodObject.SELF, o);
		// [ python object self ]
		l.add(Bytecode.makeBytecode(Bytecode.PUSH_LOCAL_CONTEXT));
		// []
		
		l.add(b = Bytecode.makeBytecode(Bytecode.PUSH));
		b.value = o.get(UserMethodObject.FUNC, o);
		// [ callable ]
		l.add(b = Bytecode.makeBytecode(Bytecode.PUSH));
		b.value = o.get(UserMethodObject.SELF, o);
		// [ callable, python object ]
		
		for (int i=0; i<args.len(); i++){
			l.add(b = Bytecode.makeBytecode(Bytecode.PUSH));
			b.value = args.valueAt(i);
			// [ callable, python object, python object*++ ]
		}
		// [ callable, python object, python object* ]
		l.add(b = Bytecode.makeBytecode(Bytecode.CALL));
		b.argc = args.len() + 1;
		// []
		l.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN));
		// [ python object ]
		l.add(Bytecode.makeBytecode(Bytecode.RETURN));
		// []
		return l;
	}

	public static boolean equals(PythonObject a, PythonObject b) {
		return a.equals(b);
	}

	public static PythonObject list2tuple(List<? extends PythonObject> list) {
		return new TupleObject(list.toArray(new PythonObject[list.size()]));
	}
}
