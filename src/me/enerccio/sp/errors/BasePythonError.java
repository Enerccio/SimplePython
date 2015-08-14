package me.enerccio.sp.errors;

import me.enerccio.sp.types.callables.ClassObject;

public class BasePythonError extends PythonException {
	private static final long serialVersionUID = 2709780173857599349L;

	public BasePythonError(ClassObject type, String message) {
		super(type, message);
	}

	public BasePythonError(ClassObject type, String message, Throwable cause) {
		super(type, message, cause);
	}

	
}
