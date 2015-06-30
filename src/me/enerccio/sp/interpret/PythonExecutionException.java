package me.enerccio.sp.interpret;

import me.enerccio.sp.types.PythonObject;

public class PythonExecutionException extends RuntimeException {
	private static final long serialVersionUID = -1679058226367596212L;
	private PythonObject exception;

	public PythonExecutionException(PythonObject o){
		super(o.toString());
		this.setException(o);
	}

	public PythonObject getException() {
		return exception;
	}

	public void setException(PythonObject exception) {
		this.exception = exception;
	}
	
	
}
