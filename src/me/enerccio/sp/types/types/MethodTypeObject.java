package me.enerccio.sp.types.types;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.ClassInstanceObject;
import me.enerccio.sp.types.callables.UserFunctionObject;
import me.enerccio.sp.types.callables.UserMethodObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class MethodTypeObject extends TypeObject {
	private static final long serialVersionUID = 6537509851545433991L;
	public static final String METHOD_CALL = "method";

	@Override
	public String getTypeIdentificator() {
		return "method";
	}

	@Override
	public PythonObject call(TupleObject args) {
		if (args.len() != 2)
			throw Utils.throwException("TypeError", "method(): wrong mumber of parameters, requires 2, got " + args.len());
		
		UserFunctionObject fnc;
		ClassInstanceObject inst;
		try {
			fnc = (UserFunctionObject) args.valueAt(0);
			inst = (ClassInstanceObject) args.valueAt(1);
		} catch (ClassCastException e){
			throw Utils.throwException("TypeError", "method(): wrong types of parameters. Parameter 1 must be function and parameter 2 must be instance");
		}
		
		UserMethodObject mo = new UserMethodObject();
		Utils.putPublic(mo, UserMethodObject.FUNC, fnc);
		Utils.putPublic(mo, UserMethodObject.SELF, inst);
		return mo;
	}
}
