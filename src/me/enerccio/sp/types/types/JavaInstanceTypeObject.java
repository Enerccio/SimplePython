package me.enerccio.sp.types.types;

import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class JavaInstanceTypeObject extends TypeObject {
	private static final long serialVersionUID = -1082259923569412321L;
	public static final String JAVA_CALL = "javainstance";

	@Override
	public String getTypeIdentificator() {
		return "java-instance";
	}

	@Override
	public PythonObject call(TupleObject args) {
		if (args.len() < 1)
			throw Utils.throwException("TypeError", "requires at least 1 parameter");
		
		PythonObject clsName = args.valueAt(0);
		if (!(clsName instanceof StringObject))
			throw Utils.throwException("TypeError", "first argument must be str");
		
		String cls = ((StringObject)clsName).value;
		
		return PythonRuntime.runtime.getJavaClass(cls, null, Utils.removeFirst(args.getObjects()));
	}

	
}
