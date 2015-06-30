package me.enerccio.sp.types.callables;

import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class UserMethodObject extends CallableObject {
	private static final long serialVersionUID = 6184279154550720464L;
	public static final String SELF = "__self__";
	public static final String FUNC = "__func__";
	
	@Override
	public PythonObject call(TupleObject args) {
		try {
			PythonInterpret.interpret.get().currentContext.push(fields.get(SELF).object);
			return ((UserFunctionObject)fields.get(FUNC).object)
					.call(new TupleObject((PythonObject[]) Utils.pushLeft(fields
							.get(SELF).object, args.getObjects())));
		} finally {
			PythonInterpret.interpret.get().currentContext.pop();
		}
	}

	@Override
	public PythonObject set(String key, PythonObject localContext,
			PythonObject value) {
		if (key.equals(SELF) || key.equals(FUNC))
			throw Utils.throwException("AttributeError", "'" + 
					Utils.run("str", Utils.run("type", this)) + "' object attribute '" + key + "' is read only");
		return super.set(key, localContext, value);
	}
}
