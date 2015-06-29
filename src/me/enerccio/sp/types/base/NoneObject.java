package me.enerccio.sp.types.base;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.PythonObject;

public class NoneObject extends PythonObject {
	private static final long serialVersionUID = 2L;

	public static final NoneObject NONE = new NoneObject();
	
	private NoneObject(){
		
	}
	
	@Override
	public boolean truthValue() {
		return false;
	}
	
	@Override
	public String toString(){
		return "None";
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
