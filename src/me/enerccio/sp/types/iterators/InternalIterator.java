package me.enerccio.sp.types.iterators;

import me.enerccio.sp.types.PythonObject;

public interface InternalIterator extends InternallyIterable {
	/** Works as next() called from python, but return null instead of throwing StopIteration */
	public PythonObject nextInternal();
	
	/** Standard python next() */
	public PythonObject next();
}
