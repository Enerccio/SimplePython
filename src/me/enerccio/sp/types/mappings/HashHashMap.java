package me.enerccio.sp.types.mappings;

import java.util.HashMap;

import me.enerccio.sp.types.PythonObject;

public class HashHashMap<T> extends HashMap<PythonProxy, T> {
	private static final long serialVersionUID = -6880977211393857008L;

	@Override
	public synchronized T get(Object key) {
		if (key instanceof PythonProxy)
			return super.get(key);
		return super.get(new PythonProxy((PythonObject) key));
	}

	@Override
	public synchronized boolean containsKey(Object key) {
		if (key instanceof PythonProxy)
			return super.containsKey(key);
		return super.containsKey(new PythonProxy((PythonObject) key));
	}

	public synchronized T put(PythonObject key, T value) {
		return super.put(new PythonProxy(key), value);
	}

	@Override
	public synchronized T remove(Object key) {
		if (key instanceof PythonProxy)
			return super.remove(key);
		return super.remove(new PythonProxy((PythonObject) key));
	}
	
}
