package me.enerccio.sp.types.pointer;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.PythonObject;

public class PointerObject extends PythonObject {
	private static final long serialVersionUID = 25L;
	
	public PointerObject(Object o){
		pointed = o;
	}
	
	private Object pointed;

	@Override
	public boolean truthValue() {
		return true;
	}

	public Object getObject() {
		return pointed;
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
