package me.enerccio.sp.types.sequences;

import java.util.ArrayList;
import java.util.List;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.utils.Utils;

public class ListObject extends MutableSequenceObject {
	private static final long serialVersionUID = 16L;

	public ListObject(){
		
	}
	
	public List<PythonObject> objects = new ArrayList<PythonObject>();
	
	@Override
	public IntObject size() {
		return new IntObject(objects.size());
	}
	
	@Override
	protected String doToString() {
		StringBuilder bd = new StringBuilder();
		bd.append("(");
		for (PythonObject o : objects)
			bd.append(o.toString() + " ");
		bd.append(")");
		return bd.toString();
	}

	@Override
	public int hashCode(){
		throw Utils.throwException("TypeError", "Unhashable type '" + Utils.run("type", this) + "'");
	}
}
