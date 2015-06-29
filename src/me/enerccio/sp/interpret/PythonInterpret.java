package me.enerccio.sp.interpret;

import me.enerccio.sp.types.PythonObject;

public class PythonInterpret {

	public static final ThreadLocal<PythonInterpret> interpret = new ThreadLocal<PythonInterpret>();
	
	public PythonInterpret(){
		bind();
	}
	
	public void bind(){
		interpret.set(this);
	}

	public PythonObject executeCall(String function, PythonObject... data) {
		// TODO Auto-generated method stub
		return null;
	}

	public PythonObject execute(PythonObject callable, PythonObject... args) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
