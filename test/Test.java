import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.io.IOUtils;


import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.runtime.ModuleProvider;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.ModuleObject;


public class Test {
	
	static class A {
		B b;
		A a;
	}
	
	static class B {
		A a;
	}
	
	public static void main(String[] args) throws Exception {
		PythonRuntime r = PythonRuntime.runtime;
		PythonInterpret i = PythonInterpret.interpret.get();
		
		File f = new File("bin\\x.spy");
		r.loadModule(new ModuleProvider("x", "x.spy", IOUtils.toByteArray(new FileInputStream(f))));
		ModuleObject o = r.getModule("x");

		
	}

}
