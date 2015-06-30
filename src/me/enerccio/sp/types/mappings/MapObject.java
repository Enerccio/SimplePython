package me.enerccio.sp.types.mappings;

import java.util.HashMap;
import java.util.Map;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.IntObject;

public class MapObject extends PythonObject {
	private static final long serialVersionUID = 20L;
	
	private Map<PythonObject, PythonObject> backingMap = new HashMap<PythonObject, PythonObject>();
	
	public IntObject size(){
		return new IntObject(backingMap.size());
	}
	
	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	public PythonObject set(String key, PythonObject localContext,
			PythonObject value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void create(String key, AccessRestrictions restrictions) {
		// TODO Auto-generated method stub
		
	}

	// Internal use only
	public boolean contains(String key) {
		return backingMap.containsKey(key);
	}
}
