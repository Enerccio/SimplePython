package me.enerccio.sp.types.callables;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.sequences.TupleObject;

public abstract class CallableObject extends PythonObject {
	private static final long serialVersionUID = 21L;
	public static final String __CALL__ = "__CALL__";
	
	public CallableObject(){
		try {
			fields.put("__call__", new AugumentedPythonObject(
					new JavaMethodObject(this, this.getClass().getMethod("call", 
							new Class<?>[]{TupleObject.class, MapObject.class})), AccessRestrictions.PUBLIC));
		} catch (NoSuchMethodException e){
			// will not happen
		}
	}
	
	public abstract PythonObject call(TupleObject args, MapObject kwargs);

	@Override
	public boolean truthValue() {
		return true;
	}

}
