package me.enerccio.sp.types.sequences;

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

	public PythonObject[] getObjects() {
		return array;
	}

	@Override
	protected String doToString() {
		StringBuilder bd = new StringBuilder();
		bd.append("(");
		for (int i=0; i<array.length; i++)
			bd.append(array[i].toString() + " ");
		bd.append(")");
		return bd.toString();
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

}
