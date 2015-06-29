package me.enerccio.sp.types.sets;

import java.util.HashSet;
import java.util.Set;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.IntObject;

public class SetObject extends AbstractSetObject {
	private static final long serialVersionUID = 19L;
	
	private Set<PythonObject> backingSet = new HashSet<PythonObject>();
	
	@Override
	public IntObject size() {
		return new IntObject(backingSet.size());
	}
}
