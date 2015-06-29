package me.enerccio.sp.types;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import me.enerccio.sp.interpret.AccessException;
import me.enerccio.sp.types.base.IntObject;

public abstract class PythonObject implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public PythonObject(){
		
	}

	protected PythonObject type;
	
	public PythonObject getType(){
		return type;
	}
	
	public IntObject getId(){
		return new IntObject(hashCode());
	}
	
	public abstract boolean truthValue();
	
	public Map<String, AugumentedPythonObject> fields = Collections.synchronizedMap(new HashMap<String, AugumentedPythonObject>());
	
	public synchronized PythonObject get(String key, PythonObject localContext) {
		if (!fields.containsKey(key))
			return null;
		AugumentedPythonObject field = fields.get(key);
		if (field.restrictions == AccessRestrictions.PRIVATE && this != localContext)
			throw new AccessException(key, this);
		return field.object;
	}

	public abstract PythonObject set(String key, PythonObject localContext,
			PythonObject value);

	public abstract void create(String key, AccessRestrictions restrictions);
}
