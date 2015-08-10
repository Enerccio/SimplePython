package me.enerccio.sp.errors;

import me.enerccio.sp.runtime.PythonRuntime;

public class IndexError extends PythonException {
	private static final long serialVersionUID = 5421505414L;
	
	public IndexError(String message) {
		super(PythonRuntime.INDEX_ERROR, message);
	}
}
