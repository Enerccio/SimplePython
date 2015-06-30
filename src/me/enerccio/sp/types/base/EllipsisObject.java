package me.enerccio.sp.types.base;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.utils.Utils;

public class EllipsisObject extends PythonObject {
	private static final long serialVersionUID = 4L;
	
	public static final EllipsisObject ELLIPSIS = new EllipsisObject(); 
	
	private EllipsisObject() {
		
	}

	@Override
	public boolean truthValue() {
		return false;
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
		return "...";
	}

}
