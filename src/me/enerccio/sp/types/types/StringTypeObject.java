package me.enerccio.sp.types.types;

import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.ClassInstanceObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class StringTypeObject extends TypeObject {
	private static final long serialVersionUID = 189813854164565772L;
	public static final String STRING_CALL = "str";

	@Override
	public String getTypeIdentificator() {
		return STRING_CALL;
	}

	@Override
	public PythonObject call(TupleObject args) {
		if (args.len() != 1)
			throw Utils.throwException("TypeError", "str(): incorrect number of parameters");
		
		PythonObject o = args.getObjects()[0];
		if (o instanceof ClassInstanceObject){
			return PythonInterpret.interpret.get().execute(false, PythonInterpret.interpret.get().executeCall("getattr", o, 
					new StringObject(ClassInstanceObject.__STR__)));
		} else
			return new StringObject(o.toString());
	}
}
