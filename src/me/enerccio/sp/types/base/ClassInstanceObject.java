package me.enerccio.sp.types.base;

import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class ClassInstanceObject extends PythonObject {
	private static final long serialVersionUID = -4687801735710617861L;
	public static final String __INIT__ = "__init__";

	@Override
	public boolean truthValue() {
		return true;
	}

	public PythonObject runMethod(String init, TupleObject args) {
		PythonObject initMethod = getInit();
		return PythonInterpret.interpret.get().invoke(initMethod, args);
	}

	private PythonObject getInit() {
		if (fields.containsKey(__INIT__))
			return fields.get(__INIT__).object;
		return Utils.run("super", fields.get(ClassObject.__CLASS__).object, this);
	}
}
