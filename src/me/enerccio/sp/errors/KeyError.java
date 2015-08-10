package me.enerccio.sp.errors;

import me.enerccio.sp.runtime.PythonRuntime;

public class KeyError extends PythonException {
	private static final long serialVersionUID = 54215546315414L;
	
	public KeyError(String message) {
		super(PythonRuntime.KEY_ERROR, message);
	}
}
