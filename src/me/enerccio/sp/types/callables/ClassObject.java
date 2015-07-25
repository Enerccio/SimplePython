package me.enerccio.sp.types.callables;

import java.util.List;

import me.enerccio.sp.interpret.ExecutionResult;
import me.enerccio.sp.interpret.PythonExecutionException;
import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.ClassInstanceObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.mappings.PythonProxy;
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
	public static final String __GETATTR__ = "__getattr__";
	
	public ClassObject(){
		
	}
	
	@Override
	public void newObject() {
		super.newObject();
		
		try {
			Utils.putPublic(this, __CALL__, new JavaMethodObject(this, this.getClass().getMethod("call", 
					new Class<?>[]{TupleObject.class}), true));
			Utils.putPublic(this, __GETATTR__, new JavaMethodObject(this, this.getClass().getMethod("getAttr", 
					new Class<?>[]{StringObject.class}), false));
		} catch (NoSuchMethodException e){
			// will not happen
		}
	};
	
	public PythonObject getAttr(StringObject o){
		try {
			return ((MapObject)fields.get(__DICT__).object).doGet(o);
		} catch (NullPointerException e){
			throw Utils.throwException("AttributeError", String.format("%s object has no attribute '%s'", Utils.run("type", this), o.value));
		}
	}

	@Override
	public PythonObject call(TupleObject args) {
		return newObject(args);
	}

	private PythonObject newObject(TupleObject args) {
		ClassInstanceObject instance = new ClassInstanceObject();
		Utils.putPublic(instance, __CLASS__, this);
		
		List<ClassObject> bbases = Utils.resolveDiamonds(this);
		
		for (ClassObject o : bbases){
			addToInstance(o.fields.get(__DICT__).object, instance);
		}
		
		int cfc = PythonInterpret.interpret.get().currentFrame.size();

		PythonInterpret.interpret.get().invoke(instance.get(ClassInstanceObject.__INIT__, instance), args);
		while (true){
			ExecutionResult res = PythonInterpret.interpret.get().executeOnce();
			if (res == ExecutionResult.INTERRUPTED)
				return instance;
			if (res == ExecutionResult.FINISHED || res == ExecutionResult.EOF)
				if (PythonInterpret.interpret.get().currentFrame.size() == cfc){
					if (PythonInterpret.interpret.get().exception() != null){
						PythonInterpret.interpret.get().currentFrame.peekLast().exception = null;
						throw new PythonExecutionException(PythonInterpret.interpret.get().exception());
					}
					return instance;
				}
		}
	}

	private void addToInstance(PythonObject s, ClassInstanceObject instance) {
		MapObject dict = (MapObject)s;
		synchronized (dict.backingMap){
			for (PythonProxy pkey : dict.backingMap.keySet()){
				PythonObject key = pkey.o;
				String kkey = ((StringObject)key).getString();
				PythonObject value = dict.backingMap.get(key);
				if (value instanceof ClassMethodObject){
					PythonObject data = value.fields.get(ClassMethodObject.__FUNC__).object;
					value = new UserMethodObject();
					value.newObject();
					Utils.putPublic(value, UserMethodObject.SELF, this);
					Utils.putPublic(value, UserMethodObject.FUNC, data);
				} else if (value instanceof StaticMethodObject){
					value = value.fields.get(StaticMethodObject.__FUNC__).object;
				} else if (value instanceof UserFunctionObject){
					PythonObject data = value;
					value = new UserMethodObject();
					value.newObject();
					Utils.putPublic(value, UserMethodObject.SELF, instance);
					Utils.putPublic(value, UserMethodObject.FUNC, data);
				} else if ((value instanceof JavaFunctionObject) && ((JavaFunctionObject)value).isWrappedMethod()){
					PythonObject data = value;
					value = new UserMethodObject();
					value.newObject();
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
		return "<class " + get(__NAME__, this).toString() + ">";
	}

}
