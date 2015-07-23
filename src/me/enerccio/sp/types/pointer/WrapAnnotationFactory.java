package me.enerccio.sp.types.pointer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class WrapAnnotationFactory extends WrapBaseFactory {
	private static final long serialVersionUID = -5142774589035715501L;

	public static @interface WrapMethod {
		
	}

	@Override
	protected List<Method> getMethods(Object instance) {
		List<Method> ml = new ArrayList<Method>();
		for (Method m : instance.getClass().getMethods()){
			if (m.isAnnotationPresent(WrapMethod.class))
				ml.add(m);
		}
		return ml;
	}

}
