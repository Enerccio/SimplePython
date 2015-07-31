package me.enerccio.sp.types.iterators;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import me.enerccio.sp.interpret.FrameObject;
import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
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
	
	public GeneratorObject(String name, FrameObject o){
		this.name = name;
		this.o = o;
	}
	
	private String name;
	private FrameObject o;
	
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
		PythonInterpret.interpret.get().currentFrame.add(o);
		o.stack.add(v);
		return NoneObject.NONE;
	}
	
	public synchronized  PythonObject throwException(ClassObject cls, PythonObject v) {
		o.exception = cls.call(new TupleObject(v), null); 
		PythonInterpret.interpret.get().currentFrame.add(o);
		return NoneObject.NONE;
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
