package me.enerccio.sp.types.base;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.sequences.TupleObject;

public abstract class NumberObject extends PythonObject {
	private static final long serialVersionUID = 8168239961379175666L;
	public static final String __INT__ = "__int__";

	public NumberObject(){
		try {
			fields.put(__INT__, new AugumentedPythonObject(
					new JavaMethodObject(this, this.getClass().getMethod("intValue", 
							new Class<?>[]{TupleObject.class, MapObject.class})), AccessRestrictions.PUBLIC));
		} catch (Exception e) {
			
		}
	}
	
	protected abstract PythonObject getIntValue();
	
	public PythonObject intValue(TupleObject args, MapObject kwargs){
		// TODO check for 0 arity
		
		return getIntValue();
	}
	
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
