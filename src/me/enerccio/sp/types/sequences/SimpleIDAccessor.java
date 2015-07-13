package me.enerccio.sp.types.sequences;

import me.enerccio.sp.types.PythonObject;

public interface SimpleIDAccessor {
	int len();
	PythonObject valueAt(int idx);
}
