package me.enerccio.sp.errors;

import me.enerccio.sp.runtime.PythonRuntime;

public class TypeError extends PythonException {
	private static final long serialVersionUID = 9845123L;
	
	public TypeError(String message) {
		super(PythonRuntime.TYPE_ERROR, message);
	}
}
