package me.enerccio.sp.types.base;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.utils.Utils;

public abstract class ContainerObject extends PythonObject {
	private static final long serialVersionUID = 1631363547607776261L;

	public static final String __CONTAINS__ = "__contains__";
	
	@Override
	public void newObject(){
		super.newObject();
		try {
			Utils.putPublic(this, __CONTAINS__, new JavaMethodObject(this, this.getClass().getMethod("contains", 
					new Class<?>[]{PythonObject.class}), false));
		} catch (NoSuchMethodException e){
			// will not happen
		}
	}
	
	public PythonObject contains(PythonObject o){
		return BoolObject.fromBoolean(containsItem(o));
	}

	protected abstract boolean containsItem(PythonObject o);
}
