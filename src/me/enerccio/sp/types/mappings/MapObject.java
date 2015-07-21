package me.enerccio.sp.types.mappings;

import java.util.Collections;
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
	private static final String __SETITEM__ = "__setitem__";
	private static final String __LEN__ = "__len__";
	
	public MapObject(){
		newObject();
	}
	
	@Override
	public void newObject() {
		super.newObject();
		
		try {
			Utils.putPublic(this, __GETITEM__, new JavaMethodObject(this, this.getClass().getMethod("getItem", 
					new Class<?>[]{TupleObject.class}), true));
			Utils.putPublic(this, __SETITEM__, new JavaMethodObject(this, this.getClass().getMethod("setItem", 
					new Class<?>[]{TupleObject.class}), true));
			Utils.putPublic(this, __LEN__, new JavaMethodObject(this, this.getClass().getMethod("len", 
					new Class<?>[]{TupleObject.class}), true));
		} catch (NoSuchMethodException e){
			// will not happen
		}
	};
	
	public Map<PythonObject, PythonObject> backingMap = 
			Collections.synchronizedMap(new HashMap<PythonObject, PythonObject>());
	
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
		return backingMap.containsKey(new StringObject(key));
	}

	public void put(String key, PythonObject value) {
		backingMap.put(new StringObject(key), value);
	}
	
	public PythonObject getItem(TupleObject a){
		if (a.size().intValue() != 1)
			throw Utils.throwException("TypeError", "__getitem__ requires 1 parameter");
		PythonObject key = a.getObjects()[0];
		if (!backingMap.containsKey(key))
			throw Utils.throwException("KeyError", "Unknown key " + key);
		return backingMap.get(key);
	}
	
	public PythonObject setItem(TupleObject a){
		if (a.size().intValue() != 2)
			throw Utils.throwException("TypeError", "__setitem__ requires 2 parameters");
		PythonObject key = a.getObjects()[0];
		PythonObject value = a.getObjects()[1];
		return backingMap.put(key, value);
	}
	
	public PythonObject len(TupleObject a){
		if (a.size().intValue() != 0)
			throw Utils.throwException("TypeError", "__len__ requires zero parameters");
		return size();
	}

	public PythonObject doGet(String str) {
		return doGet(new StringObject(str));
	}

	public PythonObject doGet(PythonObject key) {
		return backingMap.get(key);
	}

	@Override
	protected String doToString() {
		StringBuilder bd = new StringBuilder();
		bd.append("{");
		synchronized (backingMap){
			for (PythonObject key : backingMap.keySet()){
				bd.append(key.toString() + ":" + backingMap.get(key).toString() + " ");
			}
		}
		bd.append("}");
		return bd.toString();
	}

	public MapObject cloneMap() {
		MapObject c = new MapObject();
		c.backingMap.putAll(backingMap);
		return c;
	}
	
	@Override
	public IntObject getId(){
		throw Utils.throwException("TypeError", "Unhashable type '" + Utils.run("type", this) + "'");
	}
}
