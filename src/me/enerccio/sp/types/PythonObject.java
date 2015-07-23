package me.enerccio.sp.types;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.base.BoolObject;
import me.enerccio.sp.types.base.ClassInstanceObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.utils.Utils;

public abstract class PythonObject implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final String __EQ__ = "__eq__";
	public static final String __NEQ__ = "__neq__";
	public static final String __NOT__ = "__not__";
	
	public PythonObject(){
		
	}
	
	public void newObject(){
		registerObject();
	}
	
	private static Map<String, AugumentedPythonObject> sfields = Collections.synchronizedMap(new HashMap<String, AugumentedPythonObject>());
	
	static {
		try {
			sfields.put(Arithmetics.__EQ__, new AugumentedPythonObject(
					new JavaMethodObject(null, PythonObject.class.getMethod("eq", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			sfields.put(Arithmetics.__NE__, new AugumentedPythonObject(
					new JavaMethodObject(null, PythonObject.class.getMethod("ne", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			sfields.put(__NOT__, new AugumentedPythonObject(
					new JavaMethodObject(null, PythonObject.class.getMethod("not", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
		} catch (Exception e){
			e.printStackTrace();
		} 
	}
	
	protected void registerObject(){
		PythonRuntime.runtime.newInstanceInitialization(this);
		
		try {
			String m;
			
			m = Arithmetics.__EQ__;
			fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
					AccessRestrictions.PUBLIC));
			m = Arithmetics.__NE__;
			fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
					AccessRestrictions.PUBLIC));
			m = __NOT__;
			fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
					AccessRestrictions.PUBLIC));
		} catch (Exception e){
			e.printStackTrace();
		} 
	}
	
	public PythonObject eq(PythonObject other){
		return BoolObject.fromBoolean(other == this);
	}
	
	public PythonObject ne(PythonObject other){
		return BoolObject.fromBoolean(other != this);
	}
	
	public PythonObject not(PythonObject other){
		return BoolObject.fromBoolean(!truthValue());
	}

	public PythonObject getType(){
		return Utils.run("type", this);
	}
	
	public int getId(){
		return hashCode();
	}
	
	public abstract boolean truthValue();
	
	public Map<String, AugumentedPythonObject> fields = Collections.synchronizedMap(new HashMap<String, AugumentedPythonObject>());
	
	public synchronized PythonObject get(String key, PythonObject localContext) {
		if (!fields.containsKey(key))
			return null;
		AugumentedPythonObject field = fields.get(key);
		if (field.restrictions == AccessRestrictions.PRIVATE && !isPrivate(localContext))
			throw Utils.throwException("AttributeError", "access to field '" + key + "' is restricted for type '" + 
					Utils.run("str", Utils.run("type", this)) + "'");
		return field.object;
	}

	public synchronized PythonObject set(String key, PythonObject localContext,
			PythonObject value){
		if (!fields.containsKey(key))
			throw Utils.throwException("AttributeError", "'" + 
					Utils.run("str", Utils.run("type", this)) + "' object has no attribute '" + key + "'");
		AugumentedPythonObject field = fields.get(key);
		if (field.restrictions == AccessRestrictions.PRIVATE && !isPrivate(localContext))
			throw Utils.throwException("AttributeError", "access to field '" + key + "' is restricted for type '" + 
					Utils.run("str", Utils.run("type", this)) + "'");
		field.object = value;
		if (value == null)
			fields.remove(key);
		return NoneObject.NONE;
	}

	private boolean isPrivate(PythonObject localContext) {
		if (localContext == null)
			return false;
		
		if (localContext instanceof ClassObject && this instanceof ClassInstanceObject){
			return !(localContext == fields.get("__class__").object);
		}
		
		return true;
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
	
	public volatile boolean mark = false;
	public long linkName;
}
