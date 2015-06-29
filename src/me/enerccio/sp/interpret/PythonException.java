package me.enerccio.sp.interpret;

public class PythonException extends RuntimeException {
	
	public PythonException(String string) {
		super(string);
	}
	
	public PythonException(){
		super();
	}

	private static final long serialVersionUID = 1446541378354L;

}
