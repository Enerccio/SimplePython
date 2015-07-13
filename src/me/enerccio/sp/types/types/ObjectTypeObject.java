package me.enerccio.sp.types.types;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.ClassInstanceObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class ObjectTypeObject extends TypeObject {
	private static final long serialVersionUID = 4583318830595686027L;
	public static final String OBJECT_CALL = "object";
	
	@Override
	public void newObject() {
		super.newObject();
		Utils.putPublic(this, "__bases__", new TupleObject());
		Utils.putPublic(this, "__dict__", new MapObject());
	}

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
