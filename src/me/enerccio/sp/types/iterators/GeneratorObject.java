package me.enerccio.sp.types.iterators;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.enerccio.sp.interpret.FrameObject;
import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.callables.UserMethodObject;
import me.enerccio.sp.types.sequences.SequenceObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class GeneratorObject extends PythonObject {
	private static final long serialVersionUID = -3004146816129145535L;

	private static Map<String, AugumentedPythonObject> sfields = Collections.synchronizedMap(new HashMap<String, AugumentedPythonObject>());
	
	public static final String __ITER__ = SequenceObject.__ITER__;
	public static final String NEXT =  "next";
	public static final String SEND =  "send";
	public static final String THROW =  "throw";
	public static final String CLOSE =  "close";
	public static final String __DEL__ =  "__del__";

	static {
		try {
			Utils.putPublic(sfields, __ITER__, new JavaMethodObject(null, GeneratorObject.class.getMethod("__iter__", 
					new Class<?>[]{}), false));
			Utils.putPublic(sfields, NEXT, new JavaMethodObject(null, GeneratorObject.class.getMethod("next", 
					new Class<?>[]{}), false));
			Utils.putPublic(sfields, SEND, new JavaMethodObject(null, GeneratorObject.class.getMethod("send", 
					new Class<?>[]{PythonObject.class}), false));
			Utils.putPublic(sfields, THROW, new JavaMethodObject(null, GeneratorObject.class.getMethod("throwException", 
					new Class<?>[]{ClassObject.class, PythonObject.class}), false));
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public GeneratorObject(String name, List<FrameObject> o){
		this.name = name;
		this.storedFrames = o;
	}
	
	private String name;
	public List<FrameObject> storedFrames;
	
	@Override
	public void newObject() {
		super.newObject();
		
		String m;
		
		m = __ITER__;
		fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
				AccessRestrictions.PUBLIC));
		m = NEXT;
		fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
				AccessRestrictions.PUBLIC));
		m = SEND;
		fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
				AccessRestrictions.PUBLIC));
		m = THROW;
		fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
				AccessRestrictions.PUBLIC));
		
		PythonObject fnc = Utils.getGlobal("close_generator");
		
		PythonObject value = new UserMethodObject();
		value.newObject();
		Utils.putPublic(value, UserMethodObject.SELF, this);
		Utils.putPublic(value, UserMethodObject.FUNC, fnc);
		Utils.putPublic(value, UserMethodObject.ACCESSOR, NoneObject.NONE);
		
		m = CLOSE;
		fields.put(m, new AugumentedPythonObject(value, 
				AccessRestrictions.PUBLIC));
		
		m = __DEL__;
		fields.put(m, new AugumentedPythonObject(value, 
				AccessRestrictions.PUBLIC));
	}
	
	public PythonObject __iter__() {
		return this;
	}
	
	private volatile boolean nextCalled = false;
	
	public synchronized PythonObject next() {
		nextCalled = true;
		return send(NoneObject.NONE);
	}
	
	public synchronized  PythonObject send(PythonObject v) {
		if (!nextCalled && v != NoneObject.NONE)
			throw Utils.throwException("TypeError", "send(): send called before first next called"); 
		for (FrameObject o : this.storedFrames)
			PythonInterpreter.interpreter.get().currentFrame.add(o);
		this.storedFrames.get(this.storedFrames.size()-1).sendValue = v;
		return NoneObject.NONE;
	}
	
	public synchronized  PythonObject throwException(ClassObject cls, PythonObject v) {
		this.storedFrames.get(this.storedFrames.size()-1).exception = cls.call(new TupleObject(v), null); 
		return send(NoneObject.NONE);
	}
	
	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	protected String doToString() {
		return "<generator object '" + name + "' at 0x" + Integer.toHexString(hashCode()) + ">";
	}

}
