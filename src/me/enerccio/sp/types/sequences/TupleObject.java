package me.enerccio.sp.types.sequences;

import java.util.Arrays;
import java.util.List;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.base.SliceObject;
import me.enerccio.sp.utils.Utils;

public class TupleObject extends ImmutableSequenceObject  implements SimpleIDAccessor {
	private static final long serialVersionUID = 12L;
	
	public TupleObject(){
		array = new PythonObject[0];
	}

	public TupleObject(PythonObject... args){
		array = args;
		newObject();
	}
	
	private PythonObject[] array;
	
	@Override
	public IntObject size() {
		return new IntObject(array.length);
	}
	
	
	@Override
	public IntObject getId(){
		final int prime = 31;
        int result = 1;
        for (int i=0; i<array.length; i++)
	        result = prime * result
	                + ((IntObject)Utils.run("hash", array[i])).intValue();

        return new IntObject(result);
	}

	public PythonObject[] getObjects() {
		return array;
	}

	@Override
	protected String doToString() {
		if (array.length == 0)
			return "()";
		List<PythonObject> arr = Arrays.asList(array);
		String text = arr.toString();
		return "(" + text.substring(1, text.length()-1) + ")";
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
		return array.length;
	}

	@Override
	public PythonObject valueAt(int idx) {
		return array[idx];
	}

	@Override
	protected boolean containsItem(PythonObject o) {
		for (int i=0; i<len(); i++)
			if (o.equals(array[i]))
				return true;
		return false;
	}

}
