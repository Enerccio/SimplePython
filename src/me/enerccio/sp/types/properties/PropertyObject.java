package me.enerccio.sp.types.properties;

import me.enerccio.sp.types.PythonObject;

public interface PropertyObject {
	public void set(PythonObject set);

	public PythonObject get();
}
