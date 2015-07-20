package me.enerccio.sp.interpret;

import me.enerccio.sp.types.PythonObject;

public class PythonExecutionException extends RuntimeException {
	private static final long serialVersionUID = -1679058226367596212L;
	private PythonObject exception;

	public PythonExecutionException(PythonObject o){
		super(getMessage(o));
		this.setException(o);
	}
	
	public static String getMessage(PythonObject o) {
		if (o.fields.containsKey("__msg__"))
			return o.fields.get("__msg__").object.toString();
		return o.toString();
	}

	public PythonObject getException() {
		return exception;
	}

	public void setException(PythonObject exception) {
		this.exception = exception;
	}
	
	
}
