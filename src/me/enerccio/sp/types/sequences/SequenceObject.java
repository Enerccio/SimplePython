package me.enerccio.sp.types.sequences;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.ContainerObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.base.SliceObject;
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
	
	private static Map<String, AugumentedPythonObject> sfields = Collections.synchronizedMap(new HashMap<String, AugumentedPythonObject>());
	
	static {
		try {
			Utils.putPublic(sfields, __ITER__, new JavaMethodObject(null, SequenceObject.class.getMethod("__iter__", 
					new Class<?>[]{TupleObject.class}), true));
			Utils.putPublic(sfields, __GETITEM__, new JavaMethodObject(null, SequenceObject.class.getMethod("get", 
					new Class<?>[]{PythonObject.class}), false));
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void newObject() {
		super.newObject();
		
		String m;
		
		m = __ITER__;
		fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
				AccessRestrictions.PUBLIC));
		m = __GETITEM__;
		fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
				AccessRestrictions.PUBLIC));
	}
	
	public abstract PythonObject get(PythonObject key);
	
	public PythonObject __iter__(TupleObject args){
		if (args.len() > 0)
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
	
	protected int[] getSliceData(int size, PythonObject key){
		PythonObject sa = key.fields.get(SliceObject.START_ACCESSOR).object;
		PythonObject so = key.fields.get(SliceObject.STOP_ACCESSOR).object;
		PythonObject st = key.fields.get(SliceObject.STEP_ACCESSOR).object;
		
		boolean saex = sa != NoneObject.NONE;
		boolean soex = so != NoneObject.NONE;
		boolean stex = st != NoneObject.NONE;
		int sav = 0;
		int sov = size;
		int stv = 1;
		if (saex)
			sav = ((IntObject)sa).intValue();
		if (soex)
			sov = ((IntObject)so).intValue();
		if (stex)
			stv = ((IntObject)st).intValue();
		
		boolean reverse = false;
		
		if (sav < 0)
			sav = Math.max(0, size-(-(sav+1)));
		if (stv < 0){
			reverse = true;
			stv = Math.abs(stv);
		}
		if (stv == 0)
			throw Utils.throwException("ValueError", "slice step cannot be zero");
		if (sov < 0)
			sov = Math.max(0, size-(-(sov+1)));
		
		sav = Math.max(sav, 0);
		sov = Math.min(size, sov);
		
		return new int[]{sav, sov, stv, reverse ? 0 : 1};
	}
}
