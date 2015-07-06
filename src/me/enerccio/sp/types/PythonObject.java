package me.enerccio.sp.types;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.utils.Utils;

public abstract class PythonObject implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final String __EQ__ = "__eq__";
	public static final String __NEQ__ = "__neq__";
	
	public PythonObject(){
		
	}
	
	public void newObject(){
		PythonRuntime.runtime.newInstanceInitialization(this);
	}

	public PythonObject getType(){
		return Utils.run("type", this);
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
		if (field.restrictions == AccessRestrictions.PRIVATE && 
				(localContext == null || this != localContext))
			throw Utils.throwException("AttributeError", "Access to field '" + key + "' is restricted for type '" + 
					Utils.run("str", Utils.run("type", this)));
		return field.object;
	}

	public synchronized PythonObject set(String key, PythonObject localContext,
			PythonObject value){
		if (!fields.containsKey(key))
			throw Utils.throwException("AttributeError", "'" + 
					Utils.run("str", Utils.run("type", this)) + "' object has no attribute '" + key + "'");
		AugumentedPythonObject field = fields.get(key);
		if (field.restrictions == AccessRestrictions.PRIVATE && this != localContext)
			throw Utils.throwException("AttributeError", "Access to field '" + key + "' is restricted for type '" + 
					Utils.run("str", Utils.run("type", this)));
		field.object = value;
		return NoneObject.NONE;
	}

	public synchronized void create(String key, AccessRestrictions restrictions){
		if (fields.containsKey(key))
			throw Utils.throwException("AttributeError", "'" + 
					Utils.run("str", Utils.run("type", this)) + "' object already has a attribute '" + key + "'");
		AugumentedPythonObject field = new AugumentedPythonObject(NoneObject.NONE, restrictions);
		fields.put(key, field);
	}
	
	@Override
	public boolean equals(Object o){
		return o == this;
	}
	
	@Override
	public final String toString(){
		return doToString();
	}

	protected abstract String doToString();
}
