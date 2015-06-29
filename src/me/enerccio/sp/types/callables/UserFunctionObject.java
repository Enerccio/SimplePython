package me.enerccio.sp.types.callables;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.sequences.TupleObject;

public class UserFunctionObject extends CallableObject {
	private static final long serialVersionUID = 22L;

	@Override
	public PythonObject call(TupleObject args, MapObject kwargs) {
		// TODO
		return null;
	}

	@Override
	public PythonObject set(String key, PythonObject localContext,
			PythonObject value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void create(String key, AccessRestrictions restrictions) {
		// TODO Auto-generated method stub
		
	}
}
