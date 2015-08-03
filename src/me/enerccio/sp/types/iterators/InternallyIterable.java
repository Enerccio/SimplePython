package me.enerccio.sp.types.iterators;

import me.enerccio.sp.types.PythonObject;

public interface InternallyIterable {
	public PythonObject __iter__();
}
