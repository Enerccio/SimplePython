package me.enerccio.sp.runtime;

import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.ClassInstanceObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.types.IntTypeObject;
import me.enerccio.sp.types.types.StringTypeObject;
import me.enerccio.sp.types.types.TypeTypeObject;
import me.enerccio.sp.utils.Utils;

public class PythonRuntime {
	
	public static final PythonRuntime runtime = new PythonRuntime();
	public static final String GETATTR = "getattr";
	
	private PythonRuntime(){
		
	}

	public MapObject generateGlobals() {
		MapObject globals = new MapObject();
		
		globals.put(GETATTR, Utils.staticMethodCall(PythonRuntime.class, GETATTR, PythonObject.class, String.class));
		globals.put(TypeTypeObject.TYPE_CALL, new TypeTypeObject());
		globals.put(StringTypeObject.STRING_CALL, new StringTypeObject());
		globals.put(IntTypeObject.INT_CALL, new IntTypeObject());
		
		return globals;
	}
	
	public static PythonObject getattr(PythonObject o, String attribute){
		PythonObject value = o.get(attribute, PythonInterpret.interpret.get().getLocalContext());
		if (value == null){
			PythonObject getattr = getattr(o, ClassInstanceObject.__GETATTR__);
			if (getattr == null)
				throw Utils.throwException("AttributeError", "Unknown attribute " + attribute);
			value = PythonInterpret.interpret.get().execute(getattr, new StringObject(attribute));
		}
		return value;
	}
}
