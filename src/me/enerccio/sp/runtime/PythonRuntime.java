package me.enerccio.sp.runtime;

import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.types.IntTypeObject;
import me.enerccio.sp.types.types.StringTypeObject;
import me.enerccio.sp.types.types.TypeTypeObject;
import me.enerccio.sp.utils.Utils;

public class PythonRuntime {
	
	public static final PythonRuntime runtime = new PythonRuntime();
	
	private PythonRuntime(){
		
	}

	private MapObject modules = new MapObject();

	public MapObject generateGlobals() {
		MapObject globals = new MapObject();
		Utils.putPublic(globals, TypeTypeObject.TYPE_CALL, new TypeTypeObject());
		Utils.putPublic(globals, StringTypeObject.STRING_CALL, new StringTypeObject());
		Utils.putPublic(globals, IntTypeObject.INT_CALL, new IntTypeObject());
		
		return globals;
	}
}
