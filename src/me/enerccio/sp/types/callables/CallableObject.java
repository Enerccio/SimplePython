package me.enerccio.sp.types.callables;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.sequences.TupleObject;

public abstract class CallableObject extends PythonObject {
	private static final long serialVersionUID = 21L;
	public static final String __CALL__ = "__call__";
	
	public CallableObject(){
		
	}
	
	public abstract PythonObject call(TupleObject args);

	@Override
	public boolean truthValue() {
		return true;
	}

}
