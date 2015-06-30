package me.enerccio.sp.types.sequences;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.IntObject;

public class TupleObject extends ImmutableSequenceObject {
	private static final long serialVersionUID = 12L;

	public TupleObject(PythonObject... args){
		array = args;
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

}
