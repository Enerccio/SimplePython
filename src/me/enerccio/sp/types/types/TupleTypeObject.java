package me.enerccio.sp.types.types;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.sequences.TupleObject;

public class TupleTypeObject extends TypeObject {
	private static final long serialVersionUID = -5391029961115891279L;
	public static final String TUPLE_CALL = "tuple";

	@Override
	public String getTypeIdentificator() {
		return "tuple";
	}
	
	@Override
	public PythonObject call(TupleObject args) {
		return args;
	}

}
