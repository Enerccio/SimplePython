package me.enerccio.sp.types.callables;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import me.enerccio.sp.interpret.PythonExecutionException;
import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.PointerMethodIncompatibleException;
import me.enerccio.sp.utils.Utils;

public class JavaMethodObject extends CallableObject {
	private static final long serialVersionUID = 23L;

	public JavaMethodObject(Object caller, Method m, boolean noTypeConversion){
		this.caller = caller;
		this.boundHandle = m;
		m.setAccessible(true);
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
			throw Utils.throwException("TypeError", toString() + ": failed java call");
		} catch (Exception e){
			throw Utils.throwException("TypeError", toString() + ": failed java call");
		}
		
		Object[] jargs = new Object[args.size().intValue()];
		Class<?>[] types = boundHandle.getParameterTypes();
		
		if (types.length != jargs.length){
			throw Utils.throwException("TypeError", toString() + ": wrong number of parameters, expected " + types.length + ", got " + jargs.length);
		}
		
		int i=0;
		for (PythonObject o : args.getObjects()){
			try {
				jargs[i] = Utils.asJavaObject(types[i], o);
				++i;
			} catch (PointerMethodIncompatibleException e){
				throw Utils.throwException("TypeError", toString() + ": cannot convert python objects to java objects for arguments of this method");
			}
		}
		
		try {
			return Utils.cast(boundHandle.invoke(caller, jargs), boundHandle.getReturnType());
		} catch (PythonExecutionException e){
			throw e;
		} catch (InvocationTargetException e){
			if (e.getTargetException() instanceof PythonExecutionException)
				throw (RuntimeException)e.getTargetException();
			throw Utils.throwException("TypeError", toString() + ": failed java call");
		} catch (Exception e) {
			throw Utils.throwException("TypeError", toString() + ": failed java call");
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
