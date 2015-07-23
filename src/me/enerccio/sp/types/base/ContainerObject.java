package me.enerccio.sp.types.base;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.utils.Utils;

public abstract class ContainerObject extends PythonObject {
	private static final long serialVersionUID = 1631363547607776261L;

	public static final String __CONTAINS__ = "__contains__";
	
	private static Map<String, AugumentedPythonObject> sfields = Collections.synchronizedMap(new HashMap<String, AugumentedPythonObject>());
	
	static {
		try {
			Utils.putPublic(sfields, __CONTAINS__, new JavaMethodObject(null, ContainerObject.class.getMethod("contains", 
					new Class<?>[]{PythonObject.class}), false));
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void newObject(){
		super.newObject();
		String m;
		
		m = __CONTAINS__;
		fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
				AccessRestrictions.PUBLIC));
	}
	
	public PythonObject contains(PythonObject o){
		return BoolObject.fromBoolean(containsItem(o));
	}

	protected abstract boolean containsItem(PythonObject o);
}
