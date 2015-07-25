package me.enerccio.sp.types.callables;

import java.lang.reflect.Method;

public class JavaFunctionObject extends JavaMethodObject {
	private static final long serialVersionUID = 5136344028944670607L;
	private boolean isWrappedMethod = false;

	public JavaFunctionObject(Method m, boolean noTypeConversion) {
		super(null, m, noTypeConversion);
	}
	
	@Override
	protected String doToString() {
		return super.doToString();
	}

	public boolean isWrappedMethod() {
		return isWrappedMethod;
	}

	public void setWrappedMethod(boolean isWrappedMethod) {
		this.isWrappedMethod = isWrappedMethod;
	}
}
