package me.enerccio.sp.types.sequences;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.ContainerObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.utils.Utils;

public abstract class SequenceObject extends ContainerObject {
	private static final long serialVersionUID = 10L;
	
	public static final String __ITER__ = "__iter__";
	public static final String __GETITEM__ = "__getitem__";

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	public void newObject() {
		super.newObject();
		
		try {
			Utils.putPublic(this, __ITER__, new JavaMethodObject(this, this.getClass().getMethod("__iter__", 
					new Class<?>[]{TupleObject.class}), true));
			Utils.putPublic(this, __GETITEM__, new JavaMethodObject(this, this.getClass().getMethod("get", 
					new Class<?>[]{PythonObject.class}), false));
		} catch (NoSuchMethodException e){
			// will not happen
		}
	}
	
	public abstract PythonObject get(PythonObject key);
	
	public PythonObject __iter__(TupleObject args){
		if (args.size().intValue() > 0)
			throw Utils.throwException("TypeError", "__iter__(): method requires no arguments");
		return createIterator();
	}
	
	public abstract PythonObject createIterator(); 

	@Override
	protected String doToString() {
		return null;
	}

	public abstract IntObject size();
	
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
}
