package me.enerccio.sp.types.base;

import me.enerccio.sp.interpret.AccessException;
import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;

public class ClassInstanceObject extends PythonObject {
	private static final long serialVersionUID = -4687801735710617861L;

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	public synchronized PythonObject set(String key, PythonObject localContext, PythonObject value){
		if (!fields.containsKey(key))
			return null; // TODO throw python exception
		AugumentedPythonObject field = fields.get(key);
		if (field.restrictions == AccessRestrictions.PRIVATE && this != localContext)
			throw new AccessException(key, this);
		
		field.object = value;
		
		return NoneObject.NONE;
	}
	
	@Override
	public synchronized void create(String key, AccessRestrictions restrictions){
		AugumentedPythonObject field = new AugumentedPythonObject(NoneObject.NONE, restrictions);
		fields.put(key, field);
	}
}
