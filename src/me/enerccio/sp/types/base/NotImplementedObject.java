package me.enerccio.sp.types.base;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.PythonObject;

public class NotImplementedObject extends PythonObject {
	private static final long serialVersionUID = 3L;
	public static final NotImplementedObject NOT_IMPLEMENTED = new NotImplementedObject();
	
	private NotImplementedObject() {
	
	}
	
	@Override
	public boolean truthValue() {
		return true;
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
