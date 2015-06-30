package me.enerccio.sp.types.mappings;

import java.util.HashMap;
import java.util.Map;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class MapObject extends PythonObject {
	private static final long serialVersionUID = 20L;
	private static final String __GETITEM__ = "__getitem__";
	
	public MapObject(){
		try {
			Utils.putPublic(this, __GETITEM__, new JavaMethodObject(this, this.getClass().getMethod("getItem", 
					new Class<?>[]{TupleObject.class, MapObject.class})));
		} catch (NoSuchMethodException e){
			// will not happen
		}
	}
	
	public Map<PythonObject, PythonObject> backingMap = new HashMap<PythonObject, PythonObject>();
	
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
		if (!fields.containsKey(key))
			throw Utils.throwException("AttributeError", "'" + 
					Utils.run("str", Utils.run("type", this)) + "' object has no attribute '" + key + "'");
		throw Utils.throwException("AttributeError", "'" + 
				Utils.run("str", Utils.run("type", this)) + "' object attribute '" + key + "' is read only");
	}

	@Override
	public void create(String key, AccessRestrictions restrictions) {
		
	}

	// Internal use only
	public boolean contains(String key) {
		return backingMap.containsKey(key);
	}

	public void put(String key, PythonObject value) {
		backingMap.put(new StringObject(key), value);
	}
	
	public PythonObject getItem(TupleObject a, MapObject kw){
		if (a.size().intValue() != 1)
			throw Utils.throwException("TypeError", "__getitem__ requires 1 parameter");
		PythonObject key = a.getObjects()[0];
		if (!backingMap.containsKey(key))
			throw Utils.throwException("KeyError", "Unknown key " + key);
		return backingMap.get(key);
	}

	public PythonObject doGet(String str) {
		return doGet(new StringObject(str));
	}

	public PythonObject doGet(PythonObject key) {
		return backingMap.get(key);
	}
}
