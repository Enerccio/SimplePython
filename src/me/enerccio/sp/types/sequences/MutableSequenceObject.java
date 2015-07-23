package me.enerccio.sp.types.sequences;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.utils.Utils;

public abstract class MutableSequenceObject extends SequenceObject {
	private static final long serialVersionUID = 15L;
	public static final String __SETITEM__ = "__setitem__";

	private static Map<String, AugumentedPythonObject> sfields = Collections.synchronizedMap(new HashMap<String, AugumentedPythonObject>());
	
	static {
		try {
			Utils.putPublic(sfields, __SETITEM__, new JavaMethodObject(null, MutableSequenceObject.class.getMethod("set", 
					new Class<?>[]{PythonObject.class, PythonObject.class}), false));
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void newObject() {
		super.newObject();
		
		String m;
		
		m = __SETITEM__;
		fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
				AccessRestrictions.PUBLIC));
	}
	
	public abstract PythonObject set(PythonObject key, PythonObject value);
	
	
	@Override
	public int getId(){
		throw Utils.throwException("TypeError", "unhashable type '" + Utils.run("type", this) + "'");
	}
}
