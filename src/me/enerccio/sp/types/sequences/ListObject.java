package me.enerccio.sp.types.sequences;

import java.util.ArrayList;
import java.util.List;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.IntObject;

public class ListObject extends MutableSequenceObject {
	private static final long serialVersionUID = 16L;

	public ListObject(){
		
	}
	
	private List<PythonObject> objects = new ArrayList<PythonObject>();
	
	@Override
	public IntObject size() {
		return new IntObject(objects.size());
	}

}
