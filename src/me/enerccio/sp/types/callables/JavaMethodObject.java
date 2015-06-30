package me.enerccio.sp.types.callables;

import java.lang.reflect.Method;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.PointerMethodIncompatibleException;
import me.enerccio.sp.utils.Utils;

public class JavaMethodObject extends CallableObject {
	private static final long serialVersionUID = 23L;

	public JavaMethodObject(Object caller, Method m){
		this.caller = caller;
		this.boundHandle = m;
	}
	
	protected Method boundHandle;
	private Object caller;
	
	@Override
	public PythonObject call(TupleObject args) {
		// kwargs are ignored
		
		Object[] jargs = new Object[args.size().intValue()];
		Class<?>[] types = boundHandle.getParameterTypes();
		
		if (types.length != jargs.length){
			// TODO
		}
		
		int i=0;
		for (PythonObject o : args.getObjects()){
			try {
				jargs[i] = Utils.asJavaObject(types[i], o);
				++i;
			} catch (PointerMethodIncompatibleException e){
				// TODO
			}
		}
		
		try {
			return Utils.cast(boundHandle.invoke(caller, jargs), boundHandle.getReturnType());
		} catch (Exception e) {
			// TODO
			return NoneObject.NONE;
		}
	}

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

	@Override
	protected String doToString() {
		return "<java method " + boundHandle.toString() + " of object " + caller.toString() + ">";
	}
}
