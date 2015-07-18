package me.enerccio.sp.types.sequences;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.utils.Utils;

public abstract class MutableSequenceObject extends SequenceObject {
	private static final long serialVersionUID = 15L;
	public static final String __SETITEM__ = "__setitem__";

	@Override
	public void newObject() {
		super.newObject();
		
		try {
			Utils.putPublic(this, __GETITEM__, new JavaMethodObject(this, this.getClass().getMethod("set", 
					new Class<?>[]{PythonObject.class, PythonObject.class}), false));
		} catch (NoSuchMethodException e){
			// will not happen
		}
	}
	
	public abstract PythonObject set(PythonObject key, PythonObject value);
}
