package me.enerccio.sp;

import java.io.OutputStream;

import me.enerccio.sp.interpret.PythonDataSourceResolver;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.ModuleObject;

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
}
