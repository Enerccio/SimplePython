package me.enerccio.sp.types.callables;

import java.lang.reflect.Method;

public class JavaFunctionObject extends JavaMethodObject {
	private static final long serialVersionUID = 5136344028944670607L;

	public JavaFunctionObject(Method m) {
		super(null, m);
	}
}
