package me.enerccio.sp.types.pointer;

import java.lang.reflect.Method;

public class WrapAnnotationFactory extends WrapBaseFactory {
	private static final long serialVersionUID = -5142774589035715501L;

	public static @interface WrapMethod {
		
	}
	
	@Override
	public PointerObject doInitialize(Object instance) {
		PointerObject o = new PointerObject(instance);
		for (Method m : instance.getClass().getMethods()){
			if (m.getAnnotation(WrapMethod.class) != null)
				wrapMethod(m, o);
		}
		return o;
	}

}
