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
package me.enerccio.sp.runtime;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import me.enerccio.sp.compiler.PythonCompiler;
import me.enerccio.sp.interpret.CompiledBlockObject;
import me.enerccio.sp.interpret.EnvironmentObject;
import me.enerccio.sp.interpret.ExecutionResult;
import me.enerccio.sp.interpret.InternalJavaPathResolver;
import me.enerccio.sp.interpret.NoGetattrException;
import me.enerccio.sp.interpret.PythonDataSourceResolver;
import me.enerccio.sp.interpret.PythonException;
import me.enerccio.sp.interpret.PythonExecutionException;
import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.parser.pythonParser;
import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.ModuleObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.BoolObject;
import me.enerccio.sp.types.base.ClassInstanceObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.callables.JavaFunctionObject;
import me.enerccio.sp.types.callables.UserFunctionObject;
import me.enerccio.sp.types.mappings.DictObject;
import me.enerccio.sp.types.pointer.PointerFactory;
import me.enerccio.sp.types.pointer.PointerObject;
import me.enerccio.sp.types.pointer.WrapNoMethodsFactory;
import me.enerccio.sp.types.sequences.ListObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.types.system.ClassMethodObject;
import me.enerccio.sp.types.system.StaticMethodObject;
import me.enerccio.sp.types.types.BoolTypeObject;
import me.enerccio.sp.types.types.BoundFunctionTypeObject;
import me.enerccio.sp.types.types.BytecodeTypeObject;
import me.enerccio.sp.types.types.ComplexTypeObject;
import me.enerccio.sp.types.types.DictTypeObject;
import me.enerccio.sp.types.types.FunctionTypeObject;
import me.enerccio.sp.types.types.IntTypeObject;
import me.enerccio.sp.types.types.JavaCallableTypeObject;
import me.enerccio.sp.types.types.JavaInstanceTypeObject;
import me.enerccio.sp.types.types.ListTypeObject;
import me.enerccio.sp.types.types.MethodTypeObject;
import me.enerccio.sp.types.types.ObjectTypeObject;
import me.enerccio.sp.types.types.RealTypeObject;
import me.enerccio.sp.types.types.SliceTypeObject;
import me.enerccio.sp.types.types.StringTypeObject;
import me.enerccio.sp.types.types.TupleTypeObject;
import me.enerccio.sp.types.types.TypeObject;
import me.enerccio.sp.types.types.TypeTypeObject;
import me.enerccio.sp.types.types.XRangeTypeObject;
import me.enerccio.sp.utils.PointerMethodIncompatibleException;
import me.enerccio.sp.utils.Utils;

/**
 * Represents global python runtime. Contains globals and global functions. Contains loaded root modules too.
 * @author Enerccio
 *
 */
public class PythonRuntime {
	
	/** PythonRuntime is a singleton */
	public static final PythonRuntime runtime = new PythonRuntime();
	
	private PythonRuntime(){
		addFactory("", WrapNoMethodsFactory.class);
		addResolver(new InternalJavaPathResolver());
	}
	
	/** Map containing root modules, ie modules that were accessed from the root of any of resolvers */
	public Map<String, ModuleObject> root = Collections.synchronizedMap(new HashMap<String, ModuleObject>());
	private List<PythonDataSourceResolver> resolvers = new ArrayList<PythonDataSourceResolver>();
	/** object identifier key generator */
	private long key = Long.MIN_VALUE;
	
	/* related to serialization */
	private CyclicBarrier awaitBarrierEntry;
	private CyclicBarrier awaitBarrierExit;
	private volatile boolean isSaving = false;
	private volatile boolean allowedNewInterpret = true;
	
	/**
	 * Waits until creation of new interprets is possible
	 * @throws InterruptedException
	 */
	public void waitForNewInterpretAvailability() throws InterruptedException{
		if (!allowedNewInterpret)
			Thread.sleep(10);
	}
	
	/**
	 * Interpret waits here if saving is happening
	 * @param i
	 * @throws InterruptedException
	 */
	public void waitIfSaving(PythonInterpret i) throws InterruptedException {
		if (!isSaving)
			return;
		if (!i.isInterpretStoppable())
			return; // continue working
		try {
			awaitBarrierEntry.await();
			awaitBarrierExit.await();
		} catch (BrokenBarrierException e) {
			throw new InterruptedException(e.getMessage());
		}
	}
	
	/**
	 * Serializes runtime 
	 * @return
	 * @throws Exception
	 */
	public synchronized String serializeRuntime() throws Exception{
		allowedNewInterpret = false;
		int numInterprets = PythonInterpret.interprets.size();
		awaitBarrierEntry = new CyclicBarrier(numInterprets + 1); // include self
		awaitBarrierExit = new CyclicBarrier(numInterprets + 1); // include self
		isSaving = true;
		
		awaitBarrierEntry.await();
		String content = doSerializeRuntime();
		awaitBarrierExit.await();
		
		return content;
	}
	
	private String doSerializeRuntime() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Adds data source resolver
	 * @param resolver
	 */
	public synchronized void addResolver(PythonDataSourceResolver resolver){
		resolvers.add(resolver);
	}
	
	/**
	 * Called by every object to grab it's link key
	 * @param o
	 */
	public synchronized void newInstanceInitialization(PythonObject o){
		o.linkName = key++;
	}
	
	/** Returns module with given name */
	public synchronized ModuleObject getModule(String key) {
		String[] submodules = key.split(".");
		ModuleObject m = getRoot(submodules[0]);
		for (int i=1; i<submodules.length; i++)
			m = (ModuleObject)m.getField(submodules[i]);
		return m;
	}
	
	/**
	 * Returns root module with that name. If such module does not exists, it will attempt to load it.
	 * If it cannot be found, exception is thrown.
	 * @param key name of the root module
	 * @return
	 */
	public synchronized ModuleObject getRoot(String key) {
		if (!root.containsKey(key)){
			root.put(key, getModule(key, null));
		}
		return root.get(key);
	}

	/**
	 * Loads the module with given filename and returns it
	 * @param provider
	 * @return
	 */
	public ModuleObject loadModule(String filename, byte[] source) {
		String[] tmp = filename.split("/");
		String name = tmp[tmp.length - 1];
		int index = name.lastIndexOf(".");
		if (index > -1)
			name = name.substring(0, index);
		return loadModule(new ModuleProvider(name, filename, source, "whattheunhollyfuck"));
	}
	
	/**
	 * Loads the module from module provider and returns it
	 * @param provider
	 * @return
	 */
	private ModuleObject loadModule(ModuleProvider provider){
		DictObject globals = generateGlobals();
		ModuleObject mo = new ModuleObject(globals, provider);
		return mo;
	}
	
	/**
	 * returns module with name and resolve path
	 * @param name
	 * @param moduleResolvePath
	 * @return
	 */
	public synchronized ModuleObject getModule(String name, StringObject moduleResolvePath){
		if (moduleResolvePath == null)
			moduleResolvePath = new StringObject("");
		ModuleObject mo = resolveModule(name, moduleResolvePath);
		if (mo == null)
			throw Utils.throwException("ImportError", "unknown module '" + name + "' with resolve path '" + moduleResolvePath.value + "'");
		String pp = moduleResolvePath.value;
		mo.newObject();
		if (pp.equals(""))
			root.put(name, mo);
		mo.initModule();
		return mo;
	}

	/**
	 * resolve the actual module
	 * @param name
	 * @param moduleResolvePath
	 * @return
	 */
	private ModuleObject resolveModule(String name,
			StringObject moduleResolvePath) {
		ModuleProvider provider = null;
		for (PythonDataSourceResolver resolver : resolvers){
			if (provider != null)
				break;
			provider = resolver.resolve(name, moduleResolvePath.value);
		}
		if (provider != null)
			return loadModule(provider);
		return null;
	}

	/** stored globals are here */
	private static volatile DictObject globals = null;
	public static final String IS = "is";
	public static final String MRO = "mro";
	public static final String GETATTR = "getattr";
	public static final String SETATTR = "setattr";
	public static final String HASATTR = "hasattr";
	public static final String DELATTR = "delattr";
	public static final String ISINSTANCE = "isinstance";
	public static final String PRINT_JAVA = "print_java";
	public static final String PRINT_JAVA_EOL = "print_java_eol";
	public static final String STATICMETHOD = "staticmethod";
	public static final String CLASSMETHOD = "classmethod";
	public static final String CHR = "chr";
	public static final String ORD = "ord";
	public static final String APPLY = "apply";
	/**
	 * Generates globals. This is only done once but then cloned
	 * @return
	 */
	public DictObject generateGlobals() {
		if (globals == null)
			synchronized (this){
				if (globals == null){
					globals = new DictObject();
					
					EnvironmentObject e = new EnvironmentObject();
					e.newObject();
					e.add(globals);
					
					PythonInterpret.interpret.get().currentEnvironment.push(e);
					PythonObject o;
					
					globals.put("None", NoneObject.NONE);
					globals.put("True", BoolObject.TRUE);
					globals.put("False", BoolObject.FALSE);
					globals.put("globals", globals);
					globals.put(APPLY, Utils.staticMethodCall(PythonRuntime.class, APPLY, PythonObject.class, ListObject.class));
					globals.put(GETATTR, Utils.staticMethodCall(PythonRuntime.class, GETATTR, PythonObject.class, String.class));
					globals.put(HASATTR, Utils.staticMethodCall(PythonRuntime.class, HASATTR, PythonObject.class, String.class));
					globals.put(DELATTR, Utils.staticMethodCall(PythonRuntime.class, DELATTR, PythonObject.class, String.class));
					globals.put(SETATTR, Utils.staticMethodCall(PythonRuntime.class, SETATTR, PythonObject.class, String.class, PythonObject.class));
					globals.put(ISINSTANCE, Utils.staticMethodCall(PythonRuntime.class, ISINSTANCE, PythonObject.class, PythonObject.class));
					globals.put(PRINT_JAVA, Utils.staticMethodCall(PythonRuntime.class, PRINT_JAVA, PythonObject.class));
					globals.put(PRINT_JAVA_EOL, Utils.staticMethodCall(PythonRuntime.class, PRINT_JAVA_EOL));
					globals.put(CLASSMETHOD, Utils.staticMethodCall(PythonRuntime.class, CLASSMETHOD, UserFunctionObject.class));
					globals.put(STATICMETHOD, Utils.staticMethodCall(PythonRuntime.class, STATICMETHOD, UserFunctionObject.class));
					globals.put(IS, Utils.staticMethodCall(PythonRuntime.class, IS, PythonObject.class, PythonObject.class));
					globals.put(MRO, Utils.staticMethodCall(PythonRuntime.class, MRO, ClassObject.class));
					globals.put(CHR, Utils.staticMethodCall(PythonRuntime.class, CHR, IntObject.class));
					globals.put(ORD, Utils.staticMethodCall(PythonRuntime.class, ORD, StringObject.class));
					globals.put(TypeTypeObject.TYPE_CALL, o = new TypeTypeObject());
					o.newObject();
					globals.put(StringTypeObject.STRING_CALL, o = new StringTypeObject());
					o.newObject();
					globals.put(IntTypeObject.INT_CALL, o = new IntTypeObject());
					o.newObject();
					globals.put(RealTypeObject.REAL_CALL, o = new RealTypeObject());
					o.newObject();
					globals.put(BytecodeTypeObject.BYTECODE_CALL, o = new BytecodeTypeObject());
					o.newObject();
					globals.put(TupleTypeObject.TUPLE_CALL, o = new TupleTypeObject());
					o.newObject();
					globals.put(ListTypeObject.LIST_CALL, o = new ListTypeObject());
					o.newObject();
					globals.put(ObjectTypeObject.OBJECT_CALL, o = ObjectTypeObject.inst);
					o.newObject();
					globals.put(SliceTypeObject.SLICE_CALL, o = new SliceTypeObject());
					o.newObject();
					globals.put(JavaInstanceTypeObject.JAVA_CALL, o = new JavaInstanceTypeObject());
					o.newObject();
					globals.put(FunctionTypeObject.FUNCTION_CALL, o = new FunctionTypeObject());
					o.newObject();
					globals.put(DictTypeObject.DICT_CALL, o = new DictTypeObject());
					o.newObject();
					globals.put(MethodTypeObject.METHOD_CALL, o = new MethodTypeObject());
					o.newObject();
					globals.put(BoolTypeObject.BOOL_CALL, o = new BoolTypeObject());
					o.newObject();
					globals.put(JavaCallableTypeObject.JAVACALLABLE_CALL, o = new JavaCallableTypeObject());
					o.newObject();
					globals.put(ComplexTypeObject.COMPLEX_CALL, o = new ComplexTypeObject());
					o.newObject();
					globals.put(BoundFunctionTypeObject.BOUND_FUNCTION_CALL, o = new BoundFunctionTypeObject());
					o.newObject();
					globals.put(XRangeTypeObject.XRANGE_CALL, o = new XRangeTypeObject());
					o.newObject();

					
					addExceptions(globals);
					
					pythonParser p;
					try {
						p = Utils.parse(new ModuleProvider("builtin", "builtin", Utils.toByteArray(getClass().getClassLoader().getResourceAsStream("builtin.spy")), null));
					} catch (Exception e1) {
						throw new PythonException("Failed to initialize python!");
					}
					
					PythonCompiler c = new PythonCompiler();
					CompiledBlockObject builtin = c.doCompile(p.file_input(), globals, "builtin", NoneObject.NONE);
					
					PythonInterpret.interpret.get().currentEnvironment.pop();
					
					PythonInterpret.interpret.get().executeBytecode(builtin);
					while (true){
						ExecutionResult r = PythonInterpret.interpret.get().executeOnce();
						if (r == ExecutionResult.OK)
							continue;
						if (r == ExecutionResult.FINISHED)
							break;
						if (r == ExecutionResult.EOF)
							continue;
						throw new PythonException("Failed to initialize python!");
					}
				}
			}
		
		return globals.cloneMap();
	}
	
	
	protected static PythonObject apply(PythonObject callable, ListObject args){
		int cfc = PythonInterpret.interpret.get().currentFrame.size();
		TupleObject a = (TupleObject) Utils.list2tuple(args.objects);
		PythonInterpret.interpret.get().execute(false, callable, null, a.getObjects());
		return PythonInterpret.interpret.get().executeAll(cfc);
	}
	
	protected static PythonObject chr(IntObject i){
		int v = (int) i.intValue();
		if (v < 0 || v > 255)
			throw Utils.throwException("ValueError", "chr(): value outside range");
		return new StringObject(Character.toString((char)v));
	}
	
	protected static PythonObject ord(StringObject i){
		String s = i.value;
		if (s.length() != 1)
			throw Utils.throwException("ValueError", "ord(): string must be single character length");
		return IntObject.valueOf(s.charAt(0));
	}
	
	protected static PythonObject classmethod(UserFunctionObject o){
		ClassMethodObject cmo = new ClassMethodObject();
		Utils.putPublic(cmo, ClassMethodObject.__FUNC__, o);
		return cmo;
	}
	
	protected static PythonObject staticmethod(UserFunctionObject o){
		StaticMethodObject smo = new StaticMethodObject();
		Utils.putPublic(smo, StaticMethodObject.__FUNC__, o);
		return smo;
	}
	
	protected static PythonObject mro(ClassObject clazz){
		List<ClassObject> ll = Utils.resolveDiamonds(clazz);
		Collections.reverse(ll);
		TupleObject to = (TupleObject) Utils.list2tuple(ll);
		to.newObject();
		return to;
	}
	
	protected static PythonObject is(PythonObject a, PythonObject b){
		return BoolObject.fromBoolean(a == b);
	}
	
	protected static PythonObject print_java(PythonObject a){
		return print_java(a, false);
	}
	
	protected static PythonObject print_java(PythonObject a, boolean dualcall){
		if (a instanceof TupleObject && ((TupleObject)a).len() != 0 && !dualcall){
			int i=0;
			for (PythonObject o : ((TupleObject)a).getObjects()){
				++i;
				print_java(o, true);
				if (i != ((TupleObject)a).getObjects().length)
					System.out.print(" ");
			}
		} else {
			int cfc = PythonInterpret.interpret.get().currentFrame.size();
			Utils.run("str", a);
			PythonObject ret = PythonInterpret.interpret.get().executeAll(cfc);
			System.out.print(Utils.run("str", ret));
		}
		return NoneObject.NONE;
	}
	
	protected static PythonObject print_java_eol(){
		System.out.println();
		return NoneObject.NONE;
	}
	
	protected static PythonObject isinstance(PythonObject testee, PythonObject clazz){
		return doIsInstance(testee, clazz, false) ? BoolObject.TRUE : BoolObject.FALSE;
	}

	public static boolean doIsInstance(PythonObject testee, PythonObject clazz, boolean skipIgnore) {
		if (clazz instanceof ClassObject){
			return isClassInstance(testee, (ClassObject)clazz);
		}
		
		if (clazz instanceof TupleObject){
			for (PythonObject o : ((TupleObject) clazz).getObjects()){
				if (skipIgnore && !(o instanceof ClassObject))
					continue;
				if (doIsInstance(testee, o, skipIgnore))
					return true;
			}
			return false;
		}
		
		if (clazz instanceof TypeObject){
			return Utils.run("type", testee).equals(clazz);
		}
		
		throw Utils.throwException("TypeError", "isinstance() arg 2 must be a class, type, or tuple of classes and types");
	}

	private static boolean isClassInstance(PythonObject testee,
			ClassObject clazz) {
		if (!(testee instanceof ClassInstanceObject)){
			return false;
		}
		ClassObject cls = (ClassObject) Utils.get(testee, "__class__");
		return checkClassAssignable(cls, clazz);
	}

	private static boolean checkClassAssignable(ClassObject cls, ClassObject clazz) {
		if (Utils.equals(cls, clazz))
			return true;
		for (PythonObject o : ((TupleObject)Utils.get(clazz, "__bases__")).getObjects())
			if (o instanceof ClassObject){
				if (checkClassAssignable(cls, (ClassObject)o)){
					return true;
				}
			}
			
		return false;
	}

	private static final ThreadLocal<Stack<PythonObject>> accessorGetattr = new ThreadLocal<Stack<PythonObject>>(){

		@Override
		protected Stack<PythonObject> initialValue() {
			return new Stack<PythonObject>();
		}
		
	};
	
	protected static PythonObject getattr(PythonObject o, String attribute){
		return getattr(o, attribute, false);
	}
	
	protected static PythonObject getattr(PythonObject o, String attribute, boolean skip) {
		if (!attribute.equals(ClassInstanceObject.__GETATTRIBUTE__)){
				PythonObject getattr = getattr(o, ClassInstanceObject.__GETATTRIBUTE__, true);
				if (getattr != null)
					return PythonInterpret.interpret.get().execute(false, getattr, null, new StringObject(attribute));
		}
		
		PythonObject value = o.get(attribute, PythonInterpret.interpret.get().getLocalContext());
		if (value == null){
			if (skip == true)
				return null;
			
			if (accessorGetattr.get().size() != 0 && accessorGetattr.get().peek() == o){
				throw new NoGetattrException();
			}
			accessorGetattr.get().push(o);
			try {
				PythonObject getattr = getattr(o, ClassInstanceObject.__GETATTR__);
				value = PythonInterpret.interpret.get().execute(false, getattr, null, new StringObject(attribute));
			} catch (NoGetattrException e) {
				throw Utils.throwException("AttributeError", String.format("%s object has no attribute '%s'", o, attribute));
			} finally {

				accessorGetattr.get().pop();
			}
		}
		return value;
	}
	
	protected static PythonObject hasattr(PythonObject o, String attribute) {
		PythonObject value = o.get(attribute, PythonInterpret.interpret.get().getLocalContext());
		return value == null ? BoolObject.FALSE : BoolObject.TRUE;
	}
	
	protected static PythonObject delattr(PythonObject o, String attribute) {
		return setattr(o, attribute, null);
	}
	
	protected static PythonObject setattr(PythonObject o, String attribute, PythonObject v){
		if (o.get("__setattr__", PythonInterpret.interpret.get().getLocalContext()) != null){
			return PythonInterpret.interpret.get().execute(false, o.get("__setattr__", PythonInterpret.interpret.get().getLocalContext()),
					null, new StringObject(attribute), v);
		}
		if (o.get(attribute, PythonInterpret.interpret.get().getLocalContext()) == null)
			o.create(attribute, attribute.startsWith("__") && !attribute.endsWith("__") ? AccessRestrictions.PRIVATE : AccessRestrictions.PUBLIC, PythonInterpret.interpret.get().getLocalContext());
		o.set(attribute, PythonInterpret.interpret.get().getLocalContext(), v);
		return NoneObject.NONE;
	}
	
	private void addExceptions(DictObject globals) {
		DictObject base = addException(globals, "Error", null, false);
		ListObject lo = new ListObject();
		lo.newObject();
		base.backingMap.put(new StringObject("stack"), lo);
		JavaFunctionObject str = (JavaFunctionObject) Utils.staticMethodCall(PythonRuntime.class, "baseExcToStr", PythonObject.class);
		str.setWrappedMethod(true);
		base.backingMap.put(new StringObject("__str__"), str);
		
		addException(globals, "BaseException", "Error", false);
		addException(globals, "Exception", "BaseException", false);
		addException(globals, "TypeError", "Exception", true);
		addException(globals, "SyntaxError", "Exception", true);
		addException(globals, "ValueError", "Exception", true);
		addException(globals, "AttributeError", "Exception", true);
		addException(globals, "ImportError", "Exception", true);
		addException(globals, "NameError", "Exception", true);
		addException(globals, "ParseError", "Exception", true);
		addException(globals, "IndexError", "Exception", true);
		addException(globals, "InterpretError", "Exception", true);
		addException(globals, "StopIteration", "Exception", false);
		addException(globals, "LoopBreak", "Exception", false);
		addException(globals, "LoopContinue", "Exception", false);
	}


	private DictObject addException(DictObject globals, String exceptionName, String exceptionBase, boolean stringArg) {
		TypeTypeObject classCreator = (TypeTypeObject) globals.doGet(TypeTypeObject.TYPE_CALL);
		DictObject dict = new DictObject();
		
		JavaFunctionObject init = (JavaFunctionObject) Utils.staticMethodCall(true, PythonRuntime.class, "initException", TupleObject.class);
		init.setWrappedMethod(true);
		
		dict.backingMap.put(new StringObject("__init__"), init);
		
		TupleObject t1, t2;
		globals.put(exceptionName, classCreator.call(t1 = new TupleObject(new StringObject(exceptionName), t2 = (exceptionBase == null ? new TupleObject() :
				new TupleObject(globals.doGet(exceptionBase))), dict), null));
		t1.newObject();
		t2.newObject();
		return dict;
	}
	
	protected static PythonObject baseExcToStr(PythonObject e){
		return new StringObject(Utils.run("str", e.get("__CLASS__", e)) + ": " + Utils.run("str", e.get("__msg__", e)));
	}
	
	protected static PythonObject initException(TupleObject o){
		if (o.len() == 1)
			return initException(o.getObjects()[0], NoneObject.NONE);
		if (o.len() == 2)
			return initException(o.getObjects()[0], o.getObjects()[1]);
		throw Utils.throwException("TypeError", "system exception requires 0 or 1 arguments");
	}
	
	protected static PythonObject initException(PythonObject e, PythonObject text){
		Utils.putPublic(e, "__msg__", text);
		return NoneObject.NONE;
	}
	
	/*
	 * How to add new classes to the system.
	 * 
	 * First, if allowAutowraps is true, SP will try to wrap all classes returned from java (or asked to instantiate via javainstance() type)
	 * You can specify packages to disallow this autowrapping by using addExcludePackageOrClass method.
	 * 
	 * To provide which wrapping factory to be used, use addFactory method. This is hierarchical. To set for root package, use addFactory with empty string.
	 * Otherwise specify package (or class) from which the runtime will apply your specified PointerFactory.
	 * 
	 * Example:
	 * 
	 * addFactory("", WrapNoMethodsFactory.class)
	 * addFactory("org", WrapPublicFactory.class)
	 * addFactory("org.i.dont.trust.thispackage", WrapAnnotationFactory.class)
	 * 
	 * if set up like this, every class outside org package will have no methods available in python, all classes in org except for org.i.dont.trust.thispackage
	 * will have all public methods available in python, and classes in org.i.dont.trust.thispackage will only have methods with annotation available. 
	 * 
	 * You can also set up aliases for classes using addAlias.
	 * 
	 * addAlias("com.example.Class", "example") will alias example into com.example.Class. in python user can:
	 * 		
	 * 		x = javainstance("example") 
	 * 
	 * instead of 
	 * 		
	 * 		x = javainstance("com.example.Class") 
	 */
	
	private Map<String, PointerFactory> factories = Collections.synchronizedMap(new TreeMap<String, PointerFactory>());
	private boolean allowAutowraps;
	private List<String> excludedPackages = new ArrayList<String>();
	private Map<String, String> aliases = Collections.synchronizedMap(new HashMap<String, String>());
	
	/**
	 * Adds this package into excluded
	 * @param packageOrClass
	 */
	public synchronized void addExcludePackageOrClass(String packageOrClass){
		excludedPackages.add(packageOrClass);
	}
	
	/**
	 * Adds alias to the fullname
	 * @param fullName
	 * @param alias
	 */
	public synchronized void addAlias(String fullName, String alias){
		aliases.put(alias, fullName);
	}
	
	/**
	 * whether classes should be autowrapped automatically or only pre wrapped classes allowed
	 * @param allowAutowraps
	 */
	public synchronized void setAllowAutowraps(boolean allowAutowraps){
		this.allowAutowraps = allowAutowraps;
	}	
	
	/**
	 * Adds factory for current package path
	 * @param packagePath
	 * @param clazz
	 */
	public void addFactory(String packagePath, Class<? extends PointerFactory> clazz) {
		try {
			factories.put(packagePath, clazz.newInstance());
		} catch (Exception e) {
			throw Utils.throwException("TypeError", "failed to instantiate pointer factory");
		}
	}

	/**
	 * Returns pointer object for the class
	 * @param cls class name
	 * @param pointedObject null or object (if provided, it gets autowrapped)
	 * @param args
	 * @return
	 */
	public PointerObject getJavaClass(String cls, Object pointedObject, PythonObject... args) {
		if (!aliases.containsKey(cls) && !allowAutowraps)
			throw Utils.throwException("TypeError", "javainstance(): unknown java type '" + cls + "'. Type is not wrapped");
		if (!aliases.containsKey(cls))
				synchronized (aliases){
					for (String s : excludedPackages)
						if (cls.startsWith(s))
							throw Utils.throwException("TypeError", "package '" + s + "' is not allowed for automatic wrapping");
					aliases.put(cls, cls);
				}
		
		cls = aliases.get(cls);
		
		Object o;
		if (pointedObject != null)
			o = pointedObject;
		else {
			Class<?> clazz;
			try {
				clazz = Class.forName(cls);
			} catch (ClassNotFoundException e1) {
				throw Utils.throwException("TypeError", "javainstance(): unknown java type " + cls);
			}
			
			Object[] jargs = new Object[args.length];
			
			Constructor<?> selected = null;
			outer:
			for (Constructor<?> c : clazz.getConstructors()){
				Class<?>[] types = c.getParameterTypes();
				if (types.length != jargs.length)
					continue;
				
				int i=0;
				for (PythonObject oo : args){
					try {
						jargs[i] = Utils.asJavaObject(types[i], oo);
						++i;
					} catch (PointerMethodIncompatibleException e){
						continue outer;
					}
				}
				
				selected = c;
				break;
			}
			
			if (selected == null)
				throw Utils.throwException("TypeError", "javainstance(): no compatibile constructor for type " + cls + " found ");
			
			try {
				o = selected.newInstance(jargs);
			} catch (PythonExecutionException e){
				throw e;
			} catch (InvocationTargetException e){
				if (e.getTargetException() instanceof PythonExecutionException)
					throw (RuntimeException)e.getTargetException();
				throw Utils.throwException("TypeError", "javainstance(): failed java constructor call");
			} catch (Exception e) {
				throw Utils.throwException("TypeError", "javainstance(): failed java constructor call");
			}
		}
		
		PointerFactory factory = getFactory(cls);
		if (factory == null){
			throw Utils.throwException("TypeError", "javainstance(): no available factory for class " + cls);
		}
		return factory.doInitialize(o);
	}

	/**
	 * Returns factory for the class
	 * @param cls fully qualified class name
	 * @return
	 */
	private PointerFactory getFactory(String cls) {
		String[] components = cls.split("\\.");
		List<String> c = new ArrayList<String>();
		c.add("");
		c.addAll(Arrays.asList(components));
		return doGetFactory(c);
	}

	/**
	 * Returns factory for the path components
	 * @param c
	 * @return
	 */
	private PointerFactory doGetFactory(List<String> c) {
		String pkgName = "";
		PointerFactory fac = null;
		for (String component : c){
			pkgName += component;
			PointerFactory ff = factories.get(pkgName);
			if (ff == null)
				break;
			fac = ff;
		}
		return fac;
	}

	/**
	 * Returns python's "object" type
	 * @return
	 */
	public synchronized ClassObject getObject() {
		ObjectTypeObject o = (ObjectTypeObject) globals.doGet(ObjectTypeObject.OBJECT_CALL);
		return o;
	}
	
	/**
	 * Loads the module but does not return it.
	 * @param string
	 */
	public void loadModule(String string) {
		getRoot(string);
	}
	
	public PythonObject runtimeWrapper() {
		// TODO Auto-generated method stub
		return null;
	}
}

