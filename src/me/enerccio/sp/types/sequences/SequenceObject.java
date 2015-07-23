package me.enerccio.sp.types.sequences;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.ContainerObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.utils.Utils;

public abstract class SequenceObject extends ContainerObject {
	private static final long serialVersionUID = 10L;
	
	public static final String __ITER__ = "__iter__";
	public static final String __GETITEM__ = "__getitem__";

	@Override
	public boolean truthValue() {
		return true;
	}
	
	private static Map<String, AugumentedPythonObject> sfields = Collections.synchronizedMap(new HashMap<String, AugumentedPythonObject>());
	
	static {
		try {
			Utils.putPublic(sfields, __ITER__, new JavaMethodObject(null, SequenceObject.class.getMethod("__iter__", 
					new Class<?>[]{TupleObject.class}), true));
			Utils.putPublic(sfields, __GETITEM__, new JavaMethodObject(null, SequenceObject.class.getMethod("get", 
					new Class<?>[]{PythonObject.class}), false));
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void newObject() {
		super.newObject();
		
		String m;
		
		m = __ITER__;
		fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
				AccessRestrictions.PUBLIC));
		m = __GETITEM__;
		fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
				AccessRestrictions.PUBLIC));
	}
	
	public abstract PythonObject get(PythonObject key);
	
	public PythonObject __iter__(TupleObject args){
		if (args.len() > 0)
			throw Utils.throwException("TypeError", "__iter__(): method requires no arguments");
		return createIterator();
	}
	
	public abstract PythonObject createIterator(); 

	@Override
	protected String doToString() {
		return null;
	}

	public abstract IntObject size();
	
	@Override
	public PythonObject set(String key, PythonObject localContext,
			PythonObject value) {
		if (!fields.containsKey(key))
			throw Utils.throwException("AttributeError", "'" + 
					Utils.run("str", Utils.run("type", this)) + "' object has no attribute '" + key + "'");
		throw Utils.throwException("AttributeError", "'" + 
				Utils.run("str", Utils.run("type", this)) + "' object attribute '" + key + "' is read only");
	}

	@Override
	public void create(String key, AccessRestrictions restrictions) {
		
	}
}
