package me.enerccio.sp.example;

import java.io.File;

import javax.swing.JFrame;

import me.enerccio.sp.SimplePython;
import me.enerccio.sp.interpret.PythonPathResolver;
import me.enerccio.sp.types.pointer.WrapPublicFactory;


public class Example extends JFrame {
	private static final long serialVersionUID = 3581091849784539787L;

	public static void main(String[] args){
		SimplePython.initialize();
		SimplePython.setAllowAutowraps(true);
		SimplePython.addFactory("me.enerccio.sp.example", WrapPublicFactory.class);
		SimplePython.addAlias(Example.class.getName(), "example");
		SimplePython.addResolver(PythonPathResolver.make(new File("").getAbsolutePath()));
		SimplePython.getModule("example1");
	}
}
