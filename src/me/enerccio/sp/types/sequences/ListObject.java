package me.enerccio.sp.types.sequences;

import java.util.ArrayList;
import java.util.List;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.base.SliceObject;
import me.enerccio.sp.utils.Utils;

public class ListObject extends MutableSequenceObject implements SimpleIDAccessor  {
	private static final long serialVersionUID = 16L;

	public ListObject(){
		
	}
	
	public List<PythonObject> objects = new ArrayList<PythonObject>();
	
	@Override
	public IntObject size() {
		return IntObject.valueOf(objects.size());
	}
	
	@Override
	protected String doToString() {
		return objects.toString();
	}

	@Override
	public int hashCode(){
		return super.hashCode();
	}

	@Override
	public PythonObject get(PythonObject key) {
		if (key instanceof SliceObject){
			// TODO
			return null;
		} else 
		return Utils.doGet(this, key);
	}

	@Override
	public PythonObject createIterator() {
		PythonObject o = new OrderedSequenceIterator(this);
		o.newObject();
		return o;
	}

	@Override
	public int len() {
		return objects.size();
	}

	@Override
	public PythonObject valueAt(int idx) {
		return objects.get(idx);
	}

	@Override
	public PythonObject set(PythonObject key, PythonObject value) {
		
		if (key instanceof IntObject){
			int i = ((IntObject)key).intValue();
			if (i >= len() || i<-(len()))
				throw Utils.throwException("IndexError", "incorrect index, expected (" + -len() + ", " + len() + "), got " + i);
			int idx = Utils.morphAround(i, len());
			objects.set(idx, value);
		} else if (key instanceof SliceObject){
			
		} else {
			throw Utils.throwException("TypeError", "key must be int or slice");
		}
		
		return this;
	}

	@Override
	protected boolean containsItem(PythonObject o) {
		return objects.contains(o);
	}
}
