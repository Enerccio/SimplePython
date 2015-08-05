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
package me.enerccio.sp;

import java.io.OutputStream;
import java.util.Collection;

import me.enerccio.sp.interpret.PythonDataSourceResolver;
import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.sandbox.PythonSecurityManager;
import me.enerccio.sp.types.ModuleObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.CallableObject;
import me.enerccio.sp.types.pointer.PointerFactory;
import me.enerccio.sp.types.pointer.PointerFinalizer;
import me.enerccio.sp.types.properties.PropertyObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Coerce;

/**
 * SimplePython API
 * 
 * Singleton static reference api providing all the utility standard user of Simple Python would need.
 * @author Enerccio
 *
 */
public class SimplePython {

	private static PythonRuntime r;
	
	/**
	 * Initializes the API.
	 */
	public static void initialize(){
		r = PythonRuntime.runtime;
	}

	/**
	 * Adds the data source resolver. Data source resolver is the way for Simple Python to load modules.
	 * Every resolver acts as a root for some part of the python path. If you need standard python path,
	 * use PythonPathResolver. Internally, SimplePython uses InternalJavaPathResolver.
	 * @param resolver PythonDataSourceResolver instance that will be queried for a module, in the order of the 
	 * insertion. First resolver that returns the module will be the one used.
	 * @see me.enerccio.sp.interpret.PythonPathResolver
	 */
	public static void addResolve(PythonDataSourceResolver resolver){
		r.addResolver(resolver);
	}
	
	/**
	 * Adds the alias to the class. Normally, in SimplePython, you use global type javainstance to create java pointer objects.
	 * First argument to that type is string with fully qualified class name. You can provide aliases to that fully qualified name
	 * to provide a way to instantiate them easier. addAlias(Foo.class, "fooclass") will then enable user to call javainstance("fooclass") 
	 * instead of fully qualified name to Foo.
	 * @param cls class for which the alias is being made
	 * @param alias string alias for the class
	 */
	public static void addAlias(Class<?> cls, String alias){
		addAlias(cls.getName(), alias);
	}

	/**
	 * Adds the alias to the class. Normally, in SimplePython, you use global type javainstance to create java pointer objects.
	 * First argument to that type is string with fully qualified class name. You can provide aliases to that fully qualified name
	 * to provide a way to instantiate them easier. addAlias(Foo.class, "fooclass") will then enable user to call javainstance("fooclass") 
	 * instead of fully qualified name to Foo.
	 * @param class name (fully qualified class path) for which the alias is being made
	 * @param alias string alias for the class
	 */
	public static void addAlias(String name, String alias) {
		r.addAlias(name, alias);
	}
	
	/**
	 * Sets the global system out for python to this stream. Any .write() operation to sys.stdout will be done to this stream.
	 * @param os stream for the output
	 */
	public static void setSystemOut(OutputStream os){
		r.setSystemOut(os);
	}
	
	/**
	 * Sets the global system err for python to this stream. Any .write() operation to sys.stderr will be done to this stream.
	 * @param os stream for the output
	 */
	public static void setSystemErr(OutputStream os){
		r.setSystemErr(os);
	}
	
	/**
	 * Serializes the runtime into String.
	 * @return serialized form of the runtime
	 * @throws Exception
	 */
	public static String serialize() throws Exception{
		return r.serializeRuntime();
	}
	
	/**
	 * Returns module corresponding to the module path. Will create and load modules, if they do not exist.
	 * @param pythonPath module path. x.y.z -> returns module z, if exists
	 * @return module object
	 */
	public static ModuleObject getModule(String pythonPath){
		return r.getModule(pythonPath);
	}
	
	/**
	 * Inserts python value into the builtins. Therefore, value would be visible to all python. 
	 * To make java value into python value, see class Coerce.
	 * @param key string key for the name of the global variable
	 * @param value the value for the global variable in builtin. Will overwrite any global value already there
	 * @see me.enerccio.sp.utils.Coerce
	 */
	public static void injectGlobals(String key, PythonObject value){
		r.getGlobals().backingMap.put(new StringObject(key), value);
	}
	
	/**
	 * If SimplePython is set to allow autowraps, any autowraps to this package or class will not be allowed.
	 * @param path path to disallow autowraps
	 * @see SimplePython#setAllowAutowraps(boolean)
	 */
	public static void addExcludePackageOrClass(String path){
		r.addExcludePackageOrClass(path);
	}
	
	/**
	 * If set to true, will enable autowrapping any value returned from java. This is dependant on the provided factories.
	 * @param autowraps whether or not to autowrap java returned Objects into pointers
	 * @see SimplePython#addFactory(String, Class) 
	 */
	public static void setAllowAutowraps(boolean autowraps){
		r.setAllowAutowraps(autowraps);
	}
	
	/**
	 * Sets the pointer factory for the path provided and lower. When java instance is created or required to wrap into pointer object,
	 * the whole package path of that class is searched and most concrete factory is chosen that will then create the pointer object from 
	 * java Object. By default, on the root package, WrapNoMethodsFactory is used. You can use provided factories: WrapNoMethodsFactory, 
	 * WrapPublicFactory or WrapAnnotationFactory, or provide your own implementation.
	 * @param packagePath from which path this pointer factory should be used. Use empty string for root of all packages
	 * @param clazz Pointer factory that should be used
	 * @see me.enerccio.sp.types.pointer.PointerFactory
	 * @see me.enerccio.sp.types.pointer.WrapAnnotationFactory
	 * @see me.enerccio.sp.types.pointer.WrapPublicFactory
	 * @see me.enerccio.sp.types.pointer.WrapNoMethodsFactory
	 * @see SimplePython#setAllowAutowraps(boolean)
	 */
	public static void addFactory(String packagePath, Class<? extends PointerFactory> clazz){
		r.addFactory(packagePath, clazz);
	}
	
	/**
	 * Specific type of coerce that coerces collection into a tuple. By default, using Coerce with collection will coerce into a list.
	 * For other coercions from python to java, use class Coerce.
	 * @param c collection of elements to be coerced into a tuple of python objects
	 * @return tuple object with values from collection coerced (deep coercion)
	 * @see me.enerccio.sp.utils.Coerce#toPython(int)
	 * @see me.enerccio.sp.utils.Coerce#toPython(Object)
	 * @see me.enerccio.sp.utils.Coerce#toPython(Object, Class)
	 */
	public static PythonObject asTuple(Collection<?> c){
		PythonObject[] values = new PythonObject[c.size()];
		int i=0;
		for (Object o : c){
			values[i++] = Coerce.toPython(o);
		}
		
		TupleObject t = new TupleObject(values);
		t.newObject();
		return t;
	}
	
	/**
	 * Sets the field of the object to a value. This is a correct call to use, will invoke python when necessary. 
	 * @param object object which's field to set to value
	 * @param fieldName name of the field
	 * @param value value to set the field to
	 */
	public static void setField(PythonObject object, String fieldName, PythonObject value){
		PythonRuntime.setattr(object, fieldName, value);
	}
	
	/**
	 * Returns the value of the field of the object provided. This is a correct call to use, will invoke python if necessary.
	 * @param o object from which the field is requested
	 * @param fieldName name of the field
	 * @return value of the field. Will unbox properties into concrete value of the property
	 */
	public static PythonObject getField(PythonObject o, String fieldName){
		PythonObject v = PythonRuntime.getattr(o, fieldName);
		if (v instanceof PropertyObject)
			return ((PropertyObject)v).get();
		return v;
	}
	
	/**
	 * Executes the function with no arguments
	 * @param module module name (or full path to the module)
	 * @param function name of the function to execute
	 * @return value of the function call
	 */
	public static PythonObject executeFunction(String module, String function){
		return executeFunction(module, function, new PythonObject[]{});
	}
	
	/**
	 * Executes the function with arguments
	 * @param module module name (or full path to the module)
	 * @param function name of the function to execute
	 * @param args arguments for the function 
	 * @return value of the function call
	 */
	public static PythonObject executeFunction(String module, String function, PythonObject... args){
		return executeFunction(getModule(module), function, args);
	}
	
	/**
	 * Executes the function with no arguments
	 * @param module module object
	 * @param function name of the function to execute
	 * @return value of the function call
	 */
	public static PythonObject executeFunction(ModuleObject module, String function){
		return executeFunction(module, function, new PythonObject[]{});
	}
	
	/**
	 * Executes the function with no arguments
	 * @param module module object
	 * @param function name of the function to execute
	 * @param args arguments for the function 
	 * @return value of the function call
	 */
	public static PythonObject executeFunction(ModuleObject module, String function, PythonObject... args){
		CallableObject c = (CallableObject) getField(module, function);
		if (c == null)
			return null;
		
		c.call(new TupleObject(), null);
		return PythonInterpreter.interpreter.get().executeAll(0);
	}
	
	/**
	 * Finalizer is called when factory finishes the creating of pointer object. If finalizer for that particular class exists, 
	 * it will be called and value returned by the finalizer will be used as the value of wrapping that java Object.
	 * @param cls class that will be finalized by this finalizer
	 * @param finalizer the finalizer to use for the class
	 */
	public static void addFinalizer(Class<?> cls, PointerFinalizer finalizer){
		addFinalizer(cls.getName(), finalizer);
	}

	/**
	 * Finalizer is called when factory finishes the creating of pointer object. If finalizer for that particular class exists, 
	 * it will be called and value returned by the finalizer will be used as the value of wrapping that java Object.
	 * @param cls class name (fully qualified path) that will be finalized by this finalizer
	 * @param finalizer the finalizer to use for the class
	 */
	public static void addFinalizer(String name, PointerFinalizer finalizer) {
		r.addPointerFinalizer(name, finalizer);
	}
	
	/**
	 * Sets the security manager. If set to null, no security will be checked, otherwise the security manager will be queried for every 
	 * unsafe action and should decide whether action will succeed or not.
	 * @param manager new security manager, will overwrite old security manager, if any
	 */
	public static void setSecurityManager(PythonSecurityManager manager){
		r.setSecurityManager(manager);
	}
	
	/**
	 * Unloads the module from the module cache. Any new requests for that module will be loaded from the file.
	 * @param pythonPath module path. x.y.z will unload module z (and if the module is package module, will unload all submodules and subpackages of that module)
	 */
	public static void unloadModule(String pythonPath){
		r.unloadModule(pythonPath);
	}
}
