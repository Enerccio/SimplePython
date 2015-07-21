package me.enerccio.sp.types.pointer;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.utils.Utils;

public class PointerObject extends PythonObject {
	private static final long serialVersionUID = 25L;
	
	public PointerObject(Object o){
		pointed = o;
	}
	
	private Object pointed;

	@Override
	public boolean truthValue() {
		return true;
	}

	public Object getObject() {
		return pointed;
	}
	
	@Override
	public IntObject getId(){
		if (pointed == null)
			return new IntObject(0);
		return new IntObject(pointed.hashCode());
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
		return "<pointer object to " + pointed.toString() + ">";
	}
}
