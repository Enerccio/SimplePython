package me.enerccio.sp.types.types;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.ClassObject;

public abstract class TypeObject extends ClassObject {
	private static final long serialVersionUID = 5891250487396458462L;
	
	public abstract String getTypeIdentificator();
	
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
