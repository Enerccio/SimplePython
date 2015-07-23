package me.enerccio.sp.types.types;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.BoolObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class BoolTypeObject extends TypeObject {
	private static final long serialVersionUID = 6840091655061000673L;
	public static final String BOOL_CALL = "bool";
	
	@Override
	public String getTypeIdentificator() {
		return "bool";
	}

	@Override
	public PythonObject call(TupleObject args) {
		if (args.len() > 1)
			throw Utils.throwException("TypeError", "bool(): requires 0 or 1 arguments");
		if (args.len() == 1)
			return BoolObject.fromBoolean(args.valueAt(0).truthValue());
		return BoolObject.FALSE;
	}

	
}
