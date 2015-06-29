package me.enerccio.sp.interpret;

import me.enerccio.sp.types.PythonObject;

public class AccessException extends PythonException {
	private static final long serialVersionUID = 9186743218973L;

	public AccessException(String field, PythonObject o){
		super("Access to field " + field + " of " + o + " is restricted");
	}
}
