package me.enerccio.sp.types.base;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class ClassInstanceObject extends PythonObject {
	private static final long serialVersionUID = -4687801735710617861L;
	public static final String __INIT__ = "__init__";
	public static final String __STR__ = "__str__";
	public static final String __GETATTR__ = "__getattr__";
	public static final String __SETATTR__ = "__setattr__";
	public static final String __HASH__ = "__hash__";

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	protected String doToString() {
		return "<instance of " + fields.get(ClassObject.__CLASS__).object.toString() + " at 0x"
				+ Long.toHexString(getId()) + ">";
	}
	
	private static JavaMethodObject pyHash;
	static {
		try {
			pyHash = new JavaMethodObject(null, ClassInstanceObject.class.getMethod("pyHash", new Class<?>[]{TupleObject.class}), true);
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	// Should be only called when object is created, not when any other class is!
	public void initObject() {
		try {
			Utils.putPublic(this, __HASH__, pyHash.cloneWithThis(this));
		} catch (Exception e) {
			// won't happen
		}
	}
	
	public IntObject pyHash(TupleObject args){
		if (args.len() != 0)
			throw Utils.throwException("TypeError", "__hash__(): requires 0 parameters");
		return IntObject.valueOf(getId());
	}
}
