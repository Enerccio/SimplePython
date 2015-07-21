package me.enerccio.sp.types.mappings;

import java.util.HashMap;

import me.enerccio.sp.types.PythonObject;

public class HashHashMap<T> extends HashMap<PythonProxy, T> {
	private static final long serialVersionUID = -6880977211393857008L;

	@Override
	public boolean containsKey(Object key) {
		return super.containsKey(new PythonProxy((PythonObject) key));
	}

	public T put(PythonObject key, T value) {
		return super.put(new PythonProxy(key), value);
	}

	@Override
	public T remove(Object key) {
		return super.remove(new PythonProxy((PythonObject) key));
	}

}
