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

import java.io.OutputStream;
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
import me.enerccio.sp.external.FileStream;
import me.enerccio.sp.external.PrintOutputStream;
import me.enerccio.sp.interpret.CompiledBlockObject;
import me.enerccio.sp.interpret.EnvironmentObject;
import me.enerccio.sp.interpret.ExecutionResult;
import me.enerccio.sp.interpret.InternalJavaPathResolver;
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.interpret.NoGetattrException;
import me.enerccio.sp.interpret.PythonDataSourceResolver;
import me.enerccio.sp.interpret.PythonException;
import me.enerccio.sp.interpret.PythonExecutionException;
import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.parser.pythonParser;
import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.ModuleObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.BoolObject;
import me.enerccio.sp.types.base.ClassInstanceObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.callables.UserFunctionObject;
import me.enerccio.sp.types.mappings.DictObject;
import me.enerccio.sp.types.pointer.PointerFactory;
import me.enerccio.sp.types.pointer.PointerObject;
import me.enerccio.sp.types.pointer.WrapAnnotationFactory;
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
import me.enerccio.sp.utils.CastFailedException;
import me.enerccio.sp.utils.Pair;
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
		addFactory("me.enerccio.sp.external", WrapAnnotationFactory.class);
		addResolver(new InternalJavaPathResolver());
		
		addAlias(FileStream.class.getName(), "filestream");
		addAlias(PrintOutputStream.class.getName(), "sysoutstream");
	}
	
	/** Map containing root modules, ie modules that were accessed from the root of any of resolvers */
	public Map<String, ModuleContainer> root = new TreeMap<String, ModuleContainer>();
	private List<PythonDataSourceResolver> resolvers = new ArrayList<PythonDataSourceResolver>();
	/** object identifier key generator */
	private long key = Long.MIN_VALUE; 
	
	/* related to serialization */
	private CyclicBarrier awaitBarrierEntry;
	private CyclicBarrier awaitBarrierExit;
	private volatile boolean isSaving = false;
	private volatile boolean allowedNewInterpret = true;
	private OutputStream out = System.out;
	private OutputStream err = System.err;
	
	/**
	 * Waits until creation of new interprets is possible
	 * @throws InterruptedException
	 */
	public void waitForNewInterpretAvailability() throws InterruptedException{
		if (!allowedNewInterpret)
			Thread.sleep(10);
	}
	
	/**
	 * Sets the sys.stdout to wrapper to this stream
	 * @param os
	 */
	public void setSystemOut(OutputStream os){
		out = os;
	}
	
	/**
	 * Sets the sys.stderr to wrapper to this stream
	 * @param os
	 */
	public void setSystemErr(OutputStream os){
		err = os;
	}
	
	/**
	 * Interpret waits here if saving is happening
	 * @param i
	 * @throws InterruptedException
	 */
	public void waitIfSaving(PythonInterpreter i) throws InterruptedException {
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
		int numInterprets = PythonInterpreter.interprets.size();
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
		String[] submodules = key.split("\\.");
		
		ModuleObject r = null;
		String path = "";
		for (String sm : submodules){
			r = getModule(sm, new StringObject(path));
			path = aggregatePath(path, sm);
		}
		
		if (r == null)
			throw Utils.throwException("ImportError", "unknown module with path '" + key + "'");
		return r;
	}
	
	private String aggregatePath(String path, String sm) {
		if (path.equals(""))
			return sm;
		return path + "." + sm;
	}

	/**
	 * Loads the module from module provider and returns it
	 * @param provider
	 * @return
	 */
	private Pair<ModuleObject, Boolean> loadModule(ModuleProvider provider){
		DictObject globals = new DictObject();
		ModuleObject mo = new ModuleObject(globals, provider);
		return Pair.makePair(mo, provider.isPackage());
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
		
		String modulePath = moduleResolvePath.value;
		if (modulePath.equals("")){
			if (root.containsKey(name))
				return root.get(name).module;
		} else {
			ModuleContainer c = null;
			for (String pathElement : modulePath.split("\\.")){
				if (c == null)
					c = root.get(pathElement);
				else
					c = c.subpackages.get(pathElement);
			}
			if (c != null){
				if (c.submodules.containsKey(name))
					return c.submodules.get(name);
				if (c.subpackages.containsKey(name))
					return c.subpackages.get(name).module;
			}
		}
		
		Pair<ModuleObject, Boolean> data = resolveModule(name, moduleResolvePath);
		ModuleObject mo = data.getFirst();
		if (mo == null)
			throw Utils.throwException("ImportError", "unknown module '" + name + "' with resolve path '" + moduleResolvePath.value + "'");
		mo.newObject();
		
		if (!modulePath.equals("")){
			String[] submodules = modulePath.split("\\.");
			ModuleContainer c = null;
			for (String pathElement : submodules){
				if (c == null)
					c = root.get(pathElement);
				else
					c = c.subpackages.get(pathElement);
			}
			if (data.getSecond()){
				ModuleContainer newCont = new ModuleContainer();
				newCont.module = mo;
				c.subpackages.put(name, newCont);
			} else {
				c.submodules.put(name, mo);
			}
		} else {
			ModuleContainer newCont = new ModuleContainer();
			newCont.module = mo;
			root.put(name, newCont);
		}
		
		mo.initModule();
		return mo;
	}

	/**
	 * resolve the actual module
	 * @param name
	 * @param moduleResolvePath
	 * @return
	 */
	private Pair<ModuleObject, Boolean> resolveModule(String name,
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
	public static final String STATICMETHOD = "staticmethod";
	public static final String CLASSMETHOD = "classmethod";
	public static final String CHR = "chr";
	public static final String ORD = "ord";
	public static final String APPLY = "apply";
	/**
	 * Generates globals. This is only done once but then cloned
	 * @return
	 */
	public DictObject getGlobals() {
		if (globals == null)
			synchronized (this){
				if (globals == null){
					globals = new DictObject();
					
					EnvironmentObject e = new EnvironmentObject();
					e.newObject();
					e.add(globals);
					
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
					
					pythonParser p;
					try {
						p = Utils.parse(new ModuleProvider("builtin", "builtin", Utils.toByteArray(getClass().getClassLoader().getResourceAsStream("builtin.spy")), null, false));
					} catch (Exception e1) {
						throw new PythonException("Failed to initialize python!");
					}
					
					PythonCompiler c = new PythonCompiler();
					CompiledBlockObject builtin = c.doCompile(p.file_input(), globals, "builtin", NoneObject.NONE, null);
					
					PythonInterpreter.interpret.get().executeBytecode(builtin);
					while (true){
						ExecutionResult r = PythonInterpreter.interpret.get().executeOnce();
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
		
		return globals;
	}
	
	
	protected static PythonObject apply(PythonObject callable, ListObject args){
		int cfc = PythonInterpreter.interpret.get().currentFrame.size();
		TupleObject a = (TupleObject) Utils.list2tuple(args.objects);
		PythonInterpreter.interpret.get().execute(false, callable, null, a.getObjects());
		return PythonInterpreter.interpret.get().executeAll(cfc);
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
					return PythonInterpreter.interpret.get().execute(false, getattr, null, new StringObject(attribute));
		}
		
		PythonObject value = o.get(attribute, PythonInterpreter.interpret.get().getLocalContext());
		if (value == null){
			if (skip == true)
				return null;
			
			if (accessorGetattr.get().size() != 0 && accessorGetattr.get().peek() == o){
				throw new NoGetattrException();
			}
			accessorGetattr.get().push(o);
			try {
				PythonObject getattr = getattr(o, ClassInstanceObject.__GETATTR__);
				value = PythonInterpreter.interpret.get().execute(false, getattr, null, new StringObject(attribute));
			} catch (NoGetattrException e) {
				throw Utils.throwException("AttributeError", String.format("%s object has no attribute '%s'", o, attribute));
			} finally {

				accessorGetattr.get().pop();
			}
		}
		return value;
	}
	
	protected static PythonObject hasattr(PythonObject o, String attribute) {
		PythonObject value = o.get(attribute, PythonInterpreter.interpret.get().getLocalContext());
		return value == null ? BoolObject.FALSE : BoolObject.TRUE;
	}
	
	protected static PythonObject delattr(PythonObject o, String attribute) {
		return setattr(o, attribute, null);
	}
	
	protected static PythonObject setattr(PythonObject o, String attribute, PythonObject v){
		if (o.get("__setattr__", PythonInterpreter.interpret.get().getLocalContext()) != null){
			return PythonInterpreter.interpret.get().execute(false, o.get("__setattr__", PythonInterpreter.interpret.get().getLocalContext()),
					null, new StringObject(attribute), v);
		}
		if (o.get(attribute, PythonInterpreter.interpret.get().getLocalContext()) == null)
			o.create(attribute, attribute.startsWith("__") && !attribute.endsWith("__") ? AccessRestrictions.PRIVATE : AccessRestrictions.PUBLIC, PythonInterpreter.interpret.get().getLocalContext());
		o.set(attribute, PythonInterpreter.interpret.get().getLocalContext(), v);
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
	
	private FactoryResolver factories = new FactoryResolver();
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
			doAddFactory(packagePath, clazz.newInstance());
		} catch (Exception e) {
			throw Utils.throwException("TypeError", "failed to instantiate pointer factory");
		}
	}

	private void doAddFactory(String packagePath, PointerFactory newInstance) {
		factories.set(newInstance, packagePath);
	}

	/**
	 * Returns pointer object for the class
	 * @param cls class name
	 * @param pointedObject null or object (if provided, it gets autowrapped)
	 * @param args
	 * @return
	 */
	public PointerObject getJavaClass(String cls, Object pointedObject, KwArgs kwargs, PythonObject... args) {
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
			
			Object[] jargs = new Object[args.length + (kwargs != null ? 1 : 0)];
			
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
					} catch (CastFailedException e){
						continue outer;
					}
				}
				
				if (kwargs != null){
					if (!types[i].isAssignableFrom(kwargs.getClass()))
						continue;
					jargs[i] = kwargs;
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
		return factories.get(c);
	}

	/**
	 * Returns python's "object" type
	 * @return
	 */
	public synchronized ClassObject getObject() {
		ObjectTypeObject o = (ObjectTypeObject) globals.doGet(ObjectTypeObject.OBJECT_CALL);
		return o;
	}

	public OutputStream getOut() {
		return out;
	}
	
	public OutputStream getErr() {
		return err;
	}
}

