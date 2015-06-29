package me.enerccio.sp.types.types;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.sequences.TupleObject;

public abstract class ClassInstanceTypeObject extends TypeObject {
	private static final long serialVersionUID = 9009562998715295679L;
	
	@Override
	public PythonObject call(TupleObject args, MapObject kwargs) {
		if (args.size().intValue() == 1)
			return typeCoerce(args);
		else
			return newClassInstance(args);
	}

	public abstract PythonObject newClassInstance(TupleObject args);
	public abstract PythonObject typeCoerce(TupleObject args);

	@Override
	public String getTypeIdentificator() {
		return "class";
	}

}
