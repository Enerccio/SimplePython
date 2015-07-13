package me.enerccio.sp.types.callables;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class UserFunctionObject extends PythonObject {
	private static final long serialVersionUID = 22L;
	public static final String GLOBALS = "__globals__";
	public static final String CLOSURE = "__closure__";
	
	public UserFunctionObject(){
		
	}
	
	@Override
	public void newObject() {
		super.newObject();
		
		try {
			Utils.putPublic(this, CallableObject.__CALL__, new JavaMethodObject(this, this.getClass().getMethod("call", 
					new Class<?>[]{TupleObject.class}), true));
		} catch (NoSuchMethodException e){
			// will not happen
		}
	};

	public PythonObject call(TupleObject args) {
		// TODO
		return NoneObject.NONE; // returns immediately
	}

	@Override
	public PythonObject set(String key, PythonObject localContext,
			PythonObject value) {
		if (key.equals(GLOBALS) || key.equals(CLOSURE))
			throw Utils.throwException("AttributeError", "'" + 
					Utils.run("str", Utils.run("type", this)) + "' object attribute '" + key + "' is read only");
		return super.set(key, localContext, value);
	}

	@Override
	protected String doToString() {
		return "<function >"; // TODO
	}

	@Override
	public boolean truthValue() {
		return true;
	}
}
