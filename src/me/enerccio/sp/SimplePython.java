package me.enerccio.sp;

import java.io.OutputStream;

import me.enerccio.sp.interpret.PythonDataSourceResolver;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.ModuleObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.pointer.PointerFactory;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.utils.Utils;

public class SimplePython {

	private static PythonRuntime r;
	
	public static void initialize(){
		r = PythonRuntime.runtime;
	}

	public static void addResolve(PythonDataSourceResolver resolver){
		r.addResolver(resolver);
	}
	
	public static void addAlias(Class<?> cls, String alias){
		addAlias(cls.getName(), alias);
	}

	public static void addAlias(String name, String alias) {
		r.addAlias(name, alias);
	}
	
	public static void setSystemOut(OutputStream os){
		r.setSystemOut(os);
	}
	
	public static void setSystemErr(OutputStream os){
		r.setSystemErr(os);
	}
	
	public static String serialize() throws Exception{
		return r.serializeRuntime();
	}
	
	public static ModuleObject getModule(String pythonPath){
		return r.getModule(pythonPath);
	}
	
	public static void injectGlobals(String key, PythonObject value){
		r.getGlobals().backingMap.put(new StringObject(key), value);
	}
	
	public static void addExcludePackageOrClass(String path){
		r.addExcludePackageOrClass(path);
	}
	
	public static void setAllowAutowraps(boolean autowraps){
		r.setAllowAutowraps(autowraps);
	}
	
	public static void addFactory(String packagePath, Class<? extends PointerFactory> clazz){
		r.addFactory(packagePath, clazz);
	}
	
	public static PythonObject convertJava(Object java){
		// TODO: add coerce
		return r.getJavaClass(java.getClass().getName(), java, null);
	}
	
	public static void setField(PythonObject object, String fieldName, PythonObject value){
		synchronized (object){
			if (!object.fields.containsKey(fieldName))
				object.create(fieldName, AccessRestrictions.PUBLIC, null);
			object.set(fieldName, null, value);
		}
	}
	
	public static PythonObject getField(PythonObject o, String fieldName){
		return o.get(fieldName, null);
	}
}
