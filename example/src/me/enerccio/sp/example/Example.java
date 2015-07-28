package me.enerccio.sp.example;

import java.io.File;

import javax.swing.JFrame;

import me.enerccio.sp.interpret.PythonPathResolver;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.pointer.WrapPublicFactory;


public class Example extends JFrame {
	private static final long serialVersionUID = 3581091849784539787L;

	public static void main(String[] args){
		PythonRuntime r = PythonRuntime.runtime;
		r.setAllowAutowraps(true);
		r.addFactory("", WrapPublicFactory.class);
		
		r.addAlias(Example.class.getName(), "example");
		
		r.addResolver(PythonPathResolver.make(new File("").getAbsolutePath()));
		
		r.loadModule("example1");
	}
}
