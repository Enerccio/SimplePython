package me.enerccio.sp.types.types;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.ClassInstanceObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class ObjectTypeObject extends TypeObject {
	private static final long serialVersionUID = 4583318830595686027L;

	@Override
	public String getTypeIdentificator() {
		return "object";
	}
	
	@Override
	public PythonObject call(TupleObject args) {
		if (args.size().intValue() != 0)
			throw Utils.throwException("TypeError", "__call__ method requires 0 parameters");
		
		ClassInstanceObject instance = new ClassInstanceObject();
		Utils.putPublic(instance, __CLASS__, this);
		
		instance.initObject();
		
		return instance;
	}

}
