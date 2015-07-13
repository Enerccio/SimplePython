package me.enerccio.sp.types.callables;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import me.enerccio.sp.interpret.PythonExecutionException;
import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.PointerMethodIncompatibleException;
import me.enerccio.sp.utils.Utils;

public class JavaMethodObject extends CallableObject {
	private static final long serialVersionUID = 23L;

	public JavaMethodObject(Object caller, Method m, boolean noTypeConversion){
		this.caller = caller;
		this.boundHandle = m;
		this.noTypeConversion = noTypeConversion;
	}
	
	protected Method boundHandle;
	private Object caller;
	private boolean noTypeConversion;
	
	@Override
	public PythonObject call(TupleObject args) {
		try {
			if (noTypeConversion){
				return Utils.cast(boundHandle.invoke(caller, args), boundHandle.getReturnType());
			}
		} catch (PythonExecutionException e){
			throw e;
		} catch (InvocationTargetException e){
			if (e.getTargetException() instanceof PythonExecutionException)
				throw (RuntimeException)e.getTargetException();
			// TODO
						return NoneObject.NONE;
		} catch (Exception e){
			// TODO
			
			return NoneObject.NONE;
		}
		
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
		} catch (PythonExecutionException e){
			throw e;
		} catch (InvocationTargetException e){
			if (e.getTargetException() instanceof PythonExecutionException)
				throw (RuntimeException)e.getTargetException();
			// TODO
						return NoneObject.NONE;
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
