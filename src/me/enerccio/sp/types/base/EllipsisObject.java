package me.enerccio.sp.types.base;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.PythonObject;

public class EllipsisObject extends PythonObject {
	private static final long serialVersionUID = 4L;
	
	public static final EllipsisObject ELLIPSIS = new EllipsisObject(); 
	
	private EllipsisObject() {
		
	}

	@Override
	public boolean truthValue() {
		return false;
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
