package me.enerccio.sp.errors;

import me.enerccio.sp.runtime.PythonRuntime;

public class ValueError extends PythonException {
	private static final long serialVersionUID = 84512156121L;
	
	public ValueError(String message) {
		super(PythonRuntime.VALUE_ERROR, message);
	}
}
