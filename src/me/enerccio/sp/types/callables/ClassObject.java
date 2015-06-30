package me.enerccio.sp.types.callables;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.ClassInstanceObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.types.system.ClassMethodObject;
import me.enerccio.sp.types.system.StaticMethodObject;
import me.enerccio.sp.utils.Utils;

public class ClassObject extends CallableObject {
	private static final long serialVersionUID = -4002910687424344724L;
	public static final String __NAME__ = "__name__";
	public static final String __BASES__ = "__bases__";
	public static final String __DICT__ = "__dict__";
	public static final String __NEW__ = "__new__";
	public static final String __CLASS__ = "__class__";
	
	public ClassObject(){
		try {
			Utils.putPublic(this, __CALL__, new JavaMethodObject(this, this.getClass().getMethod("call", 
					new Class<?>[]{TupleObject.class, MapObject.class})));
			Utils.putPublic(this, __NEW__, new JavaMethodObject(this, this.getClass().getMethod("newInstance", 
					new Class<?>[]{TupleObject.class, MapObject.class})));
		} catch (NoSuchMethodException e){
			// will not happen
		}
	}

	@Override
	public PythonObject call(TupleObject args) {
		return newObject(args);
	}

	private PythonObject newObject(TupleObject args) {
		MapObject dict = (MapObject) fields.get(__DICT__).object;
		TupleObject bases = (TupleObject) fields.get(__BASES__).object;
		
		ClassInstanceObject instance = new ClassInstanceObject();
		Utils.putPublic(instance, __CLASS__, this);
		
		for (int i=0; i<bases.getObjects().length; i++){
			addToInstance(bases.getObjects()[i].fields.get(__BASES__).object, instance);
		}
		addToInstance(dict, instance);
		
		PythonObject ret = instance.runMethod(ClassInstanceObject.__INIT__, args);
		if (ret != NoneObject.NONE)
			throw Utils.throwException("TypeError", "__init__ returned value other than None");
		return instance;
	}

	private void addToInstance(PythonObject s, ClassInstanceObject instance) {
		
		MapObject dict = (MapObject)s;
		synchronized (dict.backingMap){
			for (PythonObject key : dict.backingMap.keySet()){
				String kkey = ((StringObject)key).getString();
				PythonObject value = dict.backingMap.get(key);
				if (value instanceof ClassMethodObject){
					PythonObject data = value.fields.get(ClassMethodObject.__FUNC__).object;
					value = new UserMethodObject();
					Utils.putPublic(value, UserMethodObject.SELF, this);
					Utils.putPublic(value, UserMethodObject.FUNC, data);
				} else if (value instanceof StaticMethodObject){
					value = value.fields.get(StaticMethodObject.__FUNC__).object;
				} else if (value instanceof UserFunctionObject){
					PythonObject data = value;
					value = new UserMethodObject();
					Utils.putPublic(value, UserMethodObject.SELF, instance);
					Utils.putPublic(value, UserMethodObject.FUNC, data);
				}
				
				AccessRestrictions ar = AccessRestrictions.PUBLIC;
				if (kkey.startsWith("__") && !kkey.endsWith("__"))
					ar = AccessRestrictions.PRIVATE;
				
				instance.fields.put(kkey, new AugumentedPythonObject(value, ar));
			}
		}
	}

	@Override
	protected String doToString() {
		return "class " + get(__NAME__, this).toString();
	}

}
