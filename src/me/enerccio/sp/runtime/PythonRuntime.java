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
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.compiler.PythonCompiler;
import me.enerccio.sp.errors.AttributeError;
import me.enerccio.sp.errors.ImportError;
import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.errors.ValueError;
import me.enerccio.sp.external.Disassembler;
import me.enerccio.sp.external.FileStream;
import me.enerccio.sp.external.FormatterAccessor;
import me.enerccio.sp.external.PrintOutputStream;
import me.enerccio.sp.external.PythonTerminator;
import me.enerccio.sp.external.PythonThread;
import me.enerccio.sp.interpret.CompiledBlockObject;
import me.enerccio.sp.interpret.EnvironmentObject;
import me.enerccio.sp.interpret.ExecutionResult;
import me.enerccio.sp.interpret.InternalDict;
import me.enerccio.sp.interpret.InternalJavaPathResolver;
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.interpret.NoGetattrException;
import me.enerccio.sp.interpret.PythonDataSourceResolver;
import me.enerccio.sp.interpret.PythonExecutionException;
import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.parser.pythonParser;
import me.enerccio.sp.sandbox.PythonSecurityManager;
import me.enerccio.sp.sandbox.PythonSecurityManager.SecureAction;
import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.ModuleObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.BoolObject;
import me.enerccio.sp.types.base.ClassInstanceObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.base.NumberObject;
import me.enerccio.sp.types.base.SliceObject;
import me.enerccio.sp.types.callables.BoundHandleObject;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.callables.JavaCongruentAggregatorObject;
import me.enerccio.sp.types.callables.JavaFunctionObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.callables.UserFunctionObject;
import me.enerccio.sp.types.callables.UserMethodObject;
import me.enerccio.sp.types.mappings.DictObject;
import me.enerccio.sp.types.mappings.StringDictObject;
import me.enerccio.sp.types.pointer.PointerFactory;
import me.enerccio.sp.types.pointer.PointerFinalizer;
import me.enerccio.sp.types.pointer.PointerObject;
import me.enerccio.sp.types.pointer.WrapAnnotationFactory;
import me.enerccio.sp.types.pointer.WrapNoMethodsFactory;
import me.enerccio.sp.types.properties.PropertyObject;
import me.enerccio.sp.types.sequences.ListObject;
import me.enerccio.sp.types.sequences.SequenceObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.types.system.ClassMethodObject;
import me.enerccio.sp.types.system.StaticMethodObject;
import me.enerccio.sp.types.types.BoolTypeObject;
import me.enerccio.sp.types.types.BoundFunctionTypeObject;
import me.enerccio.sp.types.types.BytecodeTypeObject;
import me.enerccio.sp.types.types.CompiledBlockTypeObject;
import me.enerccio.sp.types.types.ComplexTypeObject;
import me.enerccio.sp.types.types.DictTypeObject;
import me.enerccio.sp.types.types.FloatTypeObject;
import me.enerccio.sp.types.types.FunctionTypeObject;
import me.enerccio.sp.types.types.IntTypeObject;
import me.enerccio.sp.types.types.JavaCallableTypeObject;
import me.enerccio.sp.types.types.JavaInstanceTypeObject;
import me.enerccio.sp.types.types.ListTypeObject;
import me.enerccio.sp.types.types.LongTypeObject;
import me.enerccio.sp.types.types.MethodTypeObject;
import me.enerccio.sp.types.types.NoneTypeObject;
import me.enerccio.sp.types.types.ObjectTypeObject;
import me.enerccio.sp.types.types.SliceTypeObject;
import me.enerccio.sp.types.types.StringTypeObject;
import me.enerccio.sp.types.types.TupleTypeObject;
import me.enerccio.sp.types.types.TypeObject;
import me.enerccio.sp.types.types.TypeTypeObject;
import me.enerccio.sp.types.types.XRangeTypeObject;
import me.enerccio.sp.utils.CastFailedException;
import me.enerccio.sp.utils.Coerce;
import me.enerccio.sp.utils.Pair;
import me.enerccio.sp.utils.StaticTools.DiamondResolver;
import me.enerccio.sp.utils.StaticTools.IOUtils;
import me.enerccio.sp.utils.StaticTools.ParserGenerator;
import me.enerccio.sp.utils.Utils;

/**
 * Represents global python runtime. Contains globals and global functions. Contains loaded root modules too.
 * @author Enerccio
 *
 */
public class PythonRuntime {
	public static int PREALOCATED_INTEGERS = 512;	// Goes from -PREALOCATED_INTEGERS to PREALOCATED_INTEGERS  
	public static boolean USE_BIGNUM_LONG = false;	// True to use BigNum as backend for python 'long' number 
	public static boolean USE_DOUBLE_FLOAT = false;	// True to use double as backend for python 'float' number
	public static boolean USE_INT_ONLY = false;		// True to disable long completely; long(x) will return int and int arithmetic may throw TypeError on overflow  
	private PythonSecurityManager manager;
	
	public void setSecurityManager(PythonSecurityManager manager){
		this.manager = manager;
	}
	
	public void checkSandboxAction(String call, SecureAction a, Object... additionalDeciders){
		if (this.manager != null)
			this.manager.checkSandbox(a, call, additionalDeciders);
	}
	
	/** PythonRuntime is a singleton */
	public static final PythonRuntime runtime = new PythonRuntime();
	
	private PythonRuntime(){
		addFactory("", WrapNoMethodsFactory.class);
		addFactory("me.enerccio.sp.external", WrapAnnotationFactory.class);
		addResolver(new InternalJavaPathResolver());
		
		addAlias(FileStream.class.getName(), "filestream");
		addAlias(PrintOutputStream.class.getName(), "sysoutstream");
		addAlias(PythonThread.class.getName(), "jthread");
		addAlias(FormatterAccessor.class.getName(), "formatter");
		addAlias(PythonTerminator.class.getName(), "terminator");
		addAlias(Disassembler.class.getName(), "disassembler");
	}
	
	/** Map containing root modules, ie modules that were accessed from the root of any of resolvers */
	public Map<String, ModuleContainer> root = new TreeMap<String, ModuleContainer>();
	private List<PythonDataSourceResolver> resolvers = new ArrayList<PythonDataSourceResolver>();
	/** object identifier key generator */
	private volatile long key = Long.MIN_VALUE; 
	
	/* related to serialization */
	private CyclicBarrier awaitBarrierEntry;
	private CyclicBarrier awaitBarrierExit;
	private volatile boolean isSaving = false;
	private volatile boolean allowedNewInterpret = true;
	private OutputStream out = System.out;
	private OutputStream err = System.err;
	public static ClassObject ERROR;
	public static ClassObject ATTRIBUTE_ERROR;
	public static ClassObject NAME_ERROR;
	public static ClassObject STOP_ITERATION;
	public static ClassObject GENERATOR_EXIT;
	public static ClassObject INDEX_ERROR;
	public static ClassObject TYPE_ERROR;
	public static ClassObject IO_ERROR;
	public static ClassObject SYNTAX_ERROR;
	public static ClassObject VALUE_ERROR;
	public static ClassObject KEY_ERROR;
	public static ClassObject NATIVE_ERROR;
	public static ClassObject INTERPRETER_ERROR;
	public static ClassObject IMPORT_ERROR;
	public static ClassObject SANDBOX_ERROR;
	public static ClassObject AST;
	
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
		int numInterprets = PythonInterpreter.interpreters.size();
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
	public void newInstanceInitialization(PythonObject o){
		o.linkName = key++;
	}
	
	public void unloadModule(String key) {
		String name = key.substring(key.lastIndexOf('.') == 0 ? 0 : key.lastIndexOf('.')+1);
		String modulePath =  key.substring(0, key.lastIndexOf('.'));
		if (modulePath.equals("")){
			if (root.containsKey(name))
				root.remove(name);
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
					c.submodules.remove(name);
				if (c.subpackages.containsKey(name))
					c.subpackages.remove(name);
			}
		}
	}
	
	/** Returns module with given name */
	public synchronized ModuleObject getModule(String key) {
		String[] submodules = key.split("\\.");
		
		ModuleObject r = null;
		String path = "";
		for (String sm : submodules){
			r = getModule(sm, new StringObject(path, true));
			path = aggregatePath(path, sm);
		}
		
		if (r == null)
			throw new ImportError("unknown module with path '" + key + "'");
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
		ModuleObject mo = new ModuleObject(provider);
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
			moduleResolvePath = new StringObject("", true);
		
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
			throw new ImportError("unknown module '" + name + "' with resolve path '" + moduleResolvePath.value + "'");
		
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
	private static volatile StringDictObject globals = null;
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
	public static final String DIR = "dir";
	public static final String EXEC = "exec_function";
	public static final String LOCALS = "locals";
	public static final String GLOBALS = "globals";
	public static final String COMPILE = "compile";
	public static final String EVAL = "eval";
	
	/** Some basic types */
	public static final TypeObject JAVA_CALLABLE_TYPE = JavaCallableTypeObject.get();
	public static final TypeObject OBJECT_TYPE = new ObjectTypeObject();
	public static final TypeObject TYPE_TYPE = new TypeTypeObject();
	public static final TypeObject NONE_TYPE = NoneObject.TYPE;
	public static final TypeObject STRING_TYPE = new StringTypeObject();
	public static final TypeObject TUPLE_TYPE = new TupleTypeObject();
	public static final TypeObject DICT_TYPE = new DictTypeObject();
	public static final TypeObject BOOL_TYPE = new BoolTypeObject();
	public static final TypeObject INT_TYPE = new IntTypeObject();
	public static final TypeObject FUNCTION_TYPE = new FunctionTypeObject();
	public static final TypeObject BYTECODE_TYPE = new BytecodeTypeObject();
	public static final TypeObject COMPILED_BLOCK_TYPE = new CompiledBlockTypeObject();
	public static final TypeObject BOUND_FUNCTION_TYPE = new BoundFunctionTypeObject();
	public static final TypeObject METHOD_TYPE = new MethodTypeObject();
	public static final TypeObject LONG_TYPE = new LongTypeObject();
	public static final TypeObject FLOAT_TYPE = new FloatTypeObject();
	public static final TypeObject LIST_TYPE = new ListTypeObject();
	
	static {
		OBJECT_TYPE.newObject();
	}
	
	/**
	 * Generates globals. This is only done once but then cloned
	 * @return
	 */
	public StringDictObject getGlobals() {
		if (globals == null)
			synchronized (this){
				if (globals == null){
					buildingGlobals.set(true);
					globals = new StringDictObject();
					buildingGlobals.set(false);
					
					EnvironmentObject e = new EnvironmentObject();
					e.add(globals);
					
					globals.put(NoneTypeObject.NONE_TYPE_CALL, NONE_TYPE);
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
					globals.put(CHR, Utils.staticMethodCall(PythonRuntime.class, CHR, int.class));
					globals.put(ORD, Utils.staticMethodCall(PythonRuntime.class, ORD, StringObject.class));
					globals.put(DIR, Utils.staticMethodCall(true, PythonRuntime.class, DIR, TupleObject.class, KwArgs.class));
					globals.put(LOCALS, Utils.staticMethodCall(PythonRuntime.class, LOCALS));
					globals.put(GLOBALS, Utils.staticMethodCall(PythonRuntime.class, GLOBALS));
					globals.put(EXEC, Utils.staticMethodCall(PythonRuntime.class, EXEC, PythonObject.class, InternalDict.class, InternalDict.class));
					globals.put(COMPILE, Utils.staticMethodCall(PythonRuntime.class, COMPILE, PythonObject.class, StringObject.class));
					globals.put(EVAL, Utils.staticMethodCall(true, PythonRuntime.class, EVAL, TupleObject.class, KwArgs.class));
					globals.put(TypeTypeObject.TYPE_CALL, TYPE_TYPE);
					globals.put(StringTypeObject.STRING_CALL, STRING_TYPE);
					globals.put(TupleTypeObject.TUPLE_CALL, TUPLE_TYPE);
					globals.put(TupleTypeObject.MAKE_TUPLE_CALL, Utils.staticMethodCall(true, TupleTypeObject.class, "make_tuple", TupleObject.class, KwArgs.class));
					globals.put(ListTypeObject.LIST_CALL, LIST_TYPE);
					globals.put(ListTypeObject.MAKE_LIST_CALL, Utils.staticMethodCall(true, ListTypeObject.class, "make_list", TupleObject.class, KwArgs.class));
					globals.put(DictTypeObject.DICT_CALL, DICT_TYPE);
					globals.put(IntTypeObject.INT_CALL, INT_TYPE);
					globals.put(BoolTypeObject.BOOL_CALL, BOOL_TYPE);
					globals.put(ObjectTypeObject.OBJECT_CALL, OBJECT_TYPE);
					globals.put(FloatTypeObject.FLOAT_CALL, new FloatTypeObject());
					globals.put(FunctionTypeObject.FUNCTION_CALL, FUNCTION_TYPE);
					globals.put(BytecodeTypeObject.BYTECODE_CALL, BYTECODE_TYPE);
					globals.put(SliceTypeObject.SLICE_CALL, new SliceTypeObject());
					globals.put(JavaInstanceTypeObject.JAVA_CALL, new JavaInstanceTypeObject());
					globals.put(MethodTypeObject.METHOD_CALL, new MethodTypeObject());
					globals.put(JavaCallableTypeObject.JAVACALLABLE_CALL, JAVA_CALLABLE_TYPE);
					globals.put(ComplexTypeObject.COMPLEX_CALL, new ComplexTypeObject());
					globals.put(BoundFunctionTypeObject.BOUND_FUNCTION_CALL, new BoundFunctionTypeObject());
					globals.put(XRangeTypeObject.XRANGE_CALL, new XRangeTypeObject());
					globals.put(CompiledBlockTypeObject.COMPILED_CALL, new CompiledBlockTypeObject());
					
					pythonParser p;
					try {
						p = ParserGenerator.parse(new ModuleProvider("builtin", 
																	 "builtin", 
																	 IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("builtin.py")), 
																	 null, 
																	 false));
					} catch (Exception e1) {
						throw new RuntimeException("Failed to initialize python!");
					}
					
					PythonCompiler c = new PythonCompiler();
					CompiledBlockObject builtin = c.doCompile(p.file_input(), new ModuleInfo() { 
						@Override public ModuleProvider getIncludeProvider() { return null ; }
						@Override public String getName() { return "<builtin>"; }
						@Override public String getFileName() { return getName(); }
					}, null);
					
					PythonInterpreter.interpreter.get().executeBytecode(builtin);
					while (true){
						ExecutionResult r = PythonInterpreter.interpreter.get().executeOnce();
						if (r == ExecutionResult.OK)
							continue;
						if (r == ExecutionResult.FINISHED)
							break;
						if (r == ExecutionResult.EOF)
							continue;
						throw new RuntimeException("Failed to initialize python!");
					}
					
					ERROR			= (ClassObject)globals.getItem("Error");
					ATTRIBUTE_ERROR	= (ClassObject)globals.getItem("AttributeError");
					NAME_ERROR		= (ClassObject)globals.getItem("NameError");
					STOP_ITERATION	= (ClassObject)globals.getItem("StopIteration");
					GENERATOR_EXIT	= (ClassObject)globals.getItem("GeneratorExit");
					INDEX_ERROR		= (ClassObject)globals.getItem("IndexError");
					TYPE_ERROR		= (ClassObject)globals.getItem("TypeError");
					IO_ERROR		= (ClassObject)globals.getItem("IOError");
					SYNTAX_ERROR	= (ClassObject)globals.getItem("SyntaxError");
					VALUE_ERROR		= (ClassObject)globals.getItem("ValueError");
					KEY_ERROR		= (ClassObject)globals.getItem("KeyError");
					NATIVE_ERROR	= (ClassObject)globals.getItem("NativeError");
					INTERPRETER_ERROR = (ClassObject)globals.getItem("InterpreterError");
					IMPORT_ERROR	= (ClassObject)globals.getItem("ImportError");
					SANDBOX_ERROR	= (ClassObject)globals.getItem("SandboxViolationError");
					AST				= (ClassObject)globals.getItem("ast");
					
					buildingGlobals.set(false);
				}
			}
		
		return globals;
	}
	
	protected static PythonObject locals(){
		return (PythonObject) PythonInterpreter.interpreter.get().environment().getLocals();
	}
	
	protected static PythonObject globals(){
		return (PythonObject) PythonInterpreter.interpreter.get().environment().getGlobals();
	}
	
	protected static PythonObject compile(PythonObject source, StringObject filename){
		PythonRuntime.runtime.checkSandboxAction("compile", SecureAction.RUNTIME_COMPILE, source, filename);
		
		CompiledBlockObject block;
		
		if (source instanceof StringObject){
			PythonCompiler c = new PythonCompiler();
			String src = ((StringObject)source).value;

			block = c.doCompile(ParserGenerator.parseCompileFunction(src, filename.value).file_input(), filename.value);
		} else if (isderived(source, AST)){
			ListObject lo = (ListObject)PythonInterpreter.interpreter.get().execute(true, Utils.run("getattr", source, new StringObject("get_bytecode")), null);
			// mundane check
			List<PythonBytecode> pbl = new ArrayList<PythonBytecode>();
			for (PythonObject o : lo.objects){
				if (!(o instanceof PythonBytecode))
					throw new TypeError("compile(): returned bytecode from ast was not of type 'bytecode', but instead of type '" + Utils.run("typename", o) + "'");
				pbl.add((PythonBytecode)o);
			}
			block = new CompiledBlockObject(pbl);
		} else {
			PythonObject str = PythonInterpreter.interpreter.get().execute(true, Utils.run("getattr", source, new StringObject("read_all")), null);
			block = (CompiledBlockObject)  Utils.run("compile", str, filename);
		}
		
		return block;
	}
	
	protected static PythonObject exec_function(PythonObject code, InternalDict locals, InternalDict globals){
		PythonRuntime.runtime.checkSandboxAction("exec", SecureAction.RUNTIME_EVAL, code);
		
		if (locals == null){
			locals = (InternalDict) Utils.run("locals");
		}
		if (globals == null){
			globals = (InternalDict) Utils.run("globals");
		}
		
		CompiledBlockObject block;
		
		if (code instanceof CompiledBlockObject){
			block = (CompiledBlockObject) code;
		} else {
			block = (CompiledBlockObject) Utils.run("compile", code, new StringObject("<exec-source>"));
		}
		
		UserFunctionObject fnc = new UserFunctionObject();
		
		String functionName = "eval/exec-function-" + (++PythonCompiler.genFunc);
		Utils.putPublic(fnc, "__name__", new StringObject(functionName));
		
		fnc.block = block;
		
		fnc.setClosure(Arrays.asList(new InternalDict[]{locals, globals, runtime.getGlobals()}));
		Utils.putPublic(fnc, "function_defaults", new StringDictObject());
		fnc.args = new ArrayList<String>();
		
		PythonInterpreter.interpreter.get().execute(true, fnc, null);
		
		return NoneObject.NONE;
	}
	
	protected static PythonObject eval(TupleObject to, KwArgs kwargs){
		if (kwargs != null)
			kwargs.checkEmpty("eval");
		if (to.len() < 1 || to.len() > 3)
			throw new TypeError("eval(): requires 1 to 3 arguments, got " + to.len());
		try {
			String s = Coerce.toJava(to.get(0), String.class);
			InternalDict d1 = null;
			if (to.len()>1)
				d1 = Coerce.toJava(to.get(1), InternalDict.class);
			InternalDict d2 = null;
			if (to.len()>2)
				d1 = Coerce.toJava(to.get(2), InternalDict.class);
			
			return eval_function(s, d1, d2);
		} catch (CastFailedException e){
			throw new TypeError("eval(): wrong types of arguments to eval, first argument must be 'str', remaining two arguments must be 'dict' objects");
		}
	}
	
	protected static PythonObject eval_function(String code, InternalDict locals, InternalDict globals){
		PythonRuntime.runtime.checkSandboxAction("eval", SecureAction.RUNTIME_EVAL, code);
		
		if (locals == null){
			locals = (InternalDict) Utils.run("locals");
		}
		if (globals == null){
			globals = (InternalDict) Utils.run("globals");
		}
		
		CompiledBlockObject block;
		
		PythonCompiler c = new PythonCompiler();
		block = c.doCompileEval(ParserGenerator.parseEval(code).eval_input());
		
		UserFunctionObject fnc = new UserFunctionObject();
		
		String functionName = "eval/exec-function-" + (++PythonCompiler.genFunc);
		Utils.putPublic(fnc, "__name__", new StringObject(functionName));
		
		fnc.block = block;
		
		fnc.setClosure(Arrays.asList(new InternalDict[]{locals, globals, runtime.getGlobals()}));
		Utils.putPublic(fnc, "function_defaults", new StringDictObject());
		fnc.args = new ArrayList<String>();
		
		return PythonInterpreter.interpreter.get().execute(true, fnc, null);
	}
	
	public static List<String> dir(TupleObject to, KwArgs kwargs){
		if (kwargs != null)
			kwargs.checkEmpty("dir");
		if (to.len() > 1)
			throw new TypeError("dir(): requires 1 or 0 arguments, got " + to.len());
		
		PythonObject o;
		if (to.len() == 1)
			o = to.get(0);
		else
			o = Utils.run("locals");
		
		Set<String> fields = new TreeSet<String>();
		
		synchronized (o){
			synchronized (o.getEditableFields()){
				fields.addAll(o.getEditableFields().keySet());
				fields.addAll(o.getGenHandleNames());
				
				if (o.getEditableFields().containsKey("__dict__")){
					PythonObject dd = o.getEditableFields().get("__dict__").object;
					if (dd instanceof InternalDict){
						synchronized (dd){
							InternalDict d = (InternalDict)dd;
							synchronized (d){
								for (String pp : d.keySet())
									fields.add(pp);
								}
							}
						}
					}
				}
			}
		
		if (o.get("__dir__", null) != null){
			PythonObject dirCall = PythonInterpreter.interpreter.get().execute(true, o.get("__dir__", null), null);
			if (!(dirCall instanceof ListObject))
				throw new TypeError("dir(): __dir__ must return list");
			ListObject lo = (ListObject) dirCall;
			synchronized (lo.objects){
				for (PythonObject po : lo.objects){
					if (!(po instanceof StringObject))
						throw new TypeError("dir(): __dir__ returned other elements in a list than str");
					fields.add(((StringObject)po).value);
				}
			}
		}
		
		return new ArrayList<String>(fields);
	}
	
	protected static PythonObject apply(PythonObject callable, ListObject args){
		int cfc = PythonInterpreter.interpreter.get().currentFrame.size();
		TupleObject a = (TupleObject) Utils.list2tuple(args.objects, true);
		PythonInterpreter.interpreter.get().execute(false, callable, null, a.getObjects());
		return PythonInterpreter.interpreter.get().executeAll(cfc);
	}
	
	protected static PythonObject chr(int v){
		if (v < 0 || v > 255)
			throw new ValueError("chr(): value outside range");
		return new StringObject(Character.toString((char)v));
	}
	
	protected static PythonObject ord(StringObject i){
		String s = i.value;
		if (s.length() != 1)
			throw new ValueError("ord(): string must be single character length");
		return NumberObject.valueOf(s.charAt(0));
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
		List<ClassObject> ll = DiamondResolver.resolveDiamonds(clazz);
		Collections.reverse(ll);
		TupleObject to = (TupleObject) Utils.list2tuple(ll, false);
		return to;
	}
	
	protected static PythonObject is(PythonObject a, PythonObject b){
		return BoolObject.fromBoolean(a == b);
	}
	
	public static PythonObject isinstance(PythonObject testee, PythonObject clazz){
		return doIsInstance(testee, clazz, false) ? BoolObject.TRUE : BoolObject.FALSE;
	}
	
	/** Returns true if testee is ClassObject derived from clazz */
	public static boolean isderived(PythonObject testee, ClassObject clazz){
		if (!(testee instanceof ClassObject))
			return false;
		if (testee.equals(clazz))
			return true;
		SequenceObject lst;
		try {
			lst = (SequenceObject)testee.get("__bases__", null);
			if (lst == null)
				return false;
			for (int i=0; i<lst.len(); i++)
				if (isderived(lst.get(i), clazz))
					return true;
		} catch (ClassCastException e) {
			return false;
		}
		return false;
	}

	public static boolean doIsInstance(PythonObject testee, PythonObject clazz, boolean skipIgnore) {
		if (clazz instanceof ClassObject){
			ClassObject cls = (ClassObject)testee.get("__class__", null);
			return isderived((ClassObject)cls, (ClassObject)clazz);
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
		
		if (clazz instanceof TypeObject) {
			clazz.equals(getType(testee));
		}
		
		throw new TypeError("isinstance() arg 2 must be a class, type, or tuple of classes and types");
	}
	
	public static ClassObject getType(PythonObject py) {
		if (py instanceof PythonBytecode)
			return BYTECODE_TYPE;
		if (py instanceof NumberObject) {
			switch (((NumberObject)py).getNumberType()) {
				case BOOL:
					return PythonRuntime.BOOL_TYPE;
				case COMPLEX:
					return (ClassObject)Utils.getGlobal(ComplexTypeObject.COMPLEX_CALL);
				case FLOAT:
					return PythonRuntime.FLOAT_TYPE;
				case INT:
					return PythonRuntime.INT_TYPE;
				case LONG:
					return PythonRuntime.LONG_TYPE;
				}
		}
		if (py instanceof ListObject)
			return PythonRuntime.LIST_TYPE;
		if (py instanceof ClassInstanceObject) {
			PythonObject o = (ClassObject)((ClassInstanceObject)py).get(ClassObject.__CLASS__, py);
			if (o == null)
				return OBJECT_TYPE;
		}
		if (py instanceof ClassObject)
			return PythonRuntime.TYPE_TYPE;
		if (py == NoneObject.NONE)
			return NoneObject.TYPE;
		if (py instanceof SliceObject)
			return (ClassObject)Utils.getGlobal(SliceTypeObject.SLICE_CALL);
		if (py instanceof TupleObject)
			return PythonRuntime.TUPLE_TYPE;
		if (py instanceof DictObject || py instanceof StringDictObject)
			return PythonRuntime.DICT_TYPE;
		if (py instanceof StringObject)
			return PythonRuntime.STRING_TYPE;
		if (py instanceof PointerObject)
			return (ClassObject)Utils.getGlobal(JavaInstanceTypeObject.JAVA_CALL);
		if (py instanceof UserFunctionObject)
			return FUNCTION_TYPE;
		if (py instanceof UserMethodObject)
			return METHOD_TYPE;
		if (py instanceof JavaMethodObject || py instanceof JavaFunctionObject || py instanceof JavaCongruentAggregatorObject)
			return JAVA_CALLABLE_TYPE;
		if (py instanceof BoundHandleObject)
			return BOUND_FUNCTION_TYPE;
		if (py instanceof CompiledBlockObject)
			return COMPILED_BLOCK_TYPE;
		
		return OBJECT_TYPE;
	}
	
	private static final ThreadLocal<Stack<PythonObject>> accessorGetattr = new ThreadLocal<Stack<PythonObject>>(){

		@Override
		protected Stack<PythonObject> initialValue() {
			return new Stack<PythonObject>();
		}
		
	};
	
	public static PythonObject getattr(PythonObject o, String attribute){
		return getattr(o, attribute, false);
	}
	
	protected static PythonObject getattr(PythonObject o, String attribute, boolean skip) {
		if (!attribute.equals(ClassInstanceObject.__GETATTRIBUTE__)){
				PythonObject getattr = getattr(o, ClassInstanceObject.__GETATTRIBUTE__, true);
				if (getattr != null && !(o instanceof ClassObject))
					return PythonInterpreter.interpreter.get().execute(false, getattr, null, new StringObject(attribute));
		}
		
		PythonObject value = o.get(attribute, PythonInterpreter.interpreter.get().getLocalContext());
		if (value == null){
			if (skip == true)
				return null;
			
			if (accessorGetattr.get().size() != 0 && accessorGetattr.get().peek() == o){
				throw new NoGetattrException();
			}
			accessorGetattr.get().push(o);
			try {
				PythonObject getattr = getattr(o, ClassInstanceObject.__GETATTR__);
				value = PythonInterpreter.interpreter.get().execute(false, getattr, null, new StringObject(attribute));
			} catch (NoGetattrException e) {
				throw new AttributeError(String.format("%s object has no attribute '%s'", o, attribute));
			} finally {

				accessorGetattr.get().pop();
			}
		}
		return value;
	}
	
	protected static PythonObject hasattr(PythonObject o, String attribute) {
		PythonObject value = o.get(attribute, PythonInterpreter.interpreter.get().getLocalContext());
		return value == null ? BoolObject.FALSE : BoolObject.TRUE;
	}
	
	protected static PythonObject delattr(PythonObject o, String attribute) {
		return setattr(o, attribute, null);
	}
	
	public static PythonObject setattr(PythonObject o, String attribute, PythonObject v){
		if (o.get("__setattr__", PythonInterpreter.interpreter.get().getLocalContext()) != null && v != null){
			return PythonInterpreter.interpreter.get().execute(false, o.get("__setattr__", PythonInterpreter.interpreter.get().getLocalContext()),
					null, new StringObject(attribute), v);
		} else if (o.get("__delattr__", PythonInterpreter.interpreter.get().getLocalContext()) != null && v == null){
			return PythonInterpreter.interpreter.get().execute(false, o.get("__delattr__", PythonInterpreter.interpreter.get().getLocalContext()),
					null, new StringObject(attribute));
		}
		PythonObject field;
		if ((field = o.get(attribute, PythonInterpreter.interpreter.get().getLocalContext())) == null && v != null)
			o.create(attribute, attribute.startsWith("__") && !attribute.endsWith("__") ? AccessRestrictions.PRIVATE : AccessRestrictions.PUBLIC, PythonInterpreter.interpreter.get().getLocalContext());
		if (field != null && field instanceof PropertyObject){
			if (v == null)
				throw new AttributeError("attribute '" + attribute + "' is a property and can't be deleted");
			
			((PropertyObject)field).set(v);
			return NoneObject.NONE;
		}
		o.set(attribute, PythonInterpreter.interpreter.get().getLocalContext(), v);
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
	private Map<String, PointerFinalizer> augumentors = Collections.synchronizedMap(new TreeMap<String, PointerFinalizer>());
	
	/**
	 * Adds this package into excluded
	 * @param packageOrClass
	 */
	public synchronized void addExcludePackageOrClass(String packageOrClass){
		excludedPackages.add(packageOrClass);
	}
	
	public synchronized void addPointerFinalizer(String name, PointerFinalizer augumentor){
		augumentors.put(name, augumentor);
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
			throw new TypeError("failed to instantiate pointer factory");
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
	public PythonObject getJavaClass(String cls, Object pointedObject, KwArgs kwargs, PythonObject... args) {
		if (!aliases.containsKey(cls) && !allowAutowraps)
			throw new TypeError("javainstance(): unknown java type '" + cls + "'. Type is not wrapped");
		if (!aliases.containsKey(cls))
				synchronized (aliases){
					for (String s : excludedPackages)
						if (cls.startsWith(s))
							throw new TypeError("package '" + s + "' is not allowed for automatic wrapping");
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
				throw new TypeError("javainstance(): unknown java type " + cls);
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
						jargs[i] = Coerce.toJava(oo, types[i]);
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
				throw new TypeError("javainstance(): no compatibile constructor for type " + cls + " found ");
			
			try {
				o = selected.newInstance(jargs);
			} catch (PythonExecutionException e){
				throw e;
			} catch (InvocationTargetException e){
				if (e.getTargetException() instanceof PythonExecutionException)
					throw (RuntimeException)e.getTargetException();
				throw new TypeError("javainstance(): failed java constructor call");
			} catch (Exception e) {
				throw new TypeError("javainstance(): failed java constructor call");
			}
		}
		
		PointerFactory factory = getFactory(cls);
		if (factory == null){
			throw new TypeError("javainstance(): no available factory for class " + cls);
		}
		PointerObject ptr = factory.doInitialize(o);
		if (augumentors.containsKey(o.getClass().getName())){
			return augumentors.get(o.getClass().getName()).finalizePointer(ptr);
		} else
			return ptr;
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

	private AtomicBoolean buildingGlobals = new AtomicBoolean(false);
	public boolean buildingGlobals() {
		return buildingGlobals.get();
	}
}

