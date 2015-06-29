package me.enerccio.sp.types.sets;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.IntObject;

public abstract class AbstractSetObject extends PythonObject {
	private static final long serialVersionUID = 18L;

	@Override
	public boolean truthValue() {
		return true;
	}

	public abstract IntObject size();
	
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
