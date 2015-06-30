package me.enerccio.sp.types.system;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.utils.Utils;

public class StaticMethodObject extends PythonObject {
	private static final long serialVersionUID = -7257861263236747558L;
	public static final String __FUNC__ = "__FUNC__";
	
	@Override
	public boolean truthValue() {
		return true;
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
		return "<static method of " + fields.get(__FUNC__).toString() + ">";
	}
}
