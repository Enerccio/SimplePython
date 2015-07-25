package me.enerccio.sp.types.pointer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class WrapPublicFactory extends WrapBaseFactory implements PointerFactory {
	private static final long serialVersionUID = 693487950048251692L;

	@Override
	protected List<Method> getMethods(Object instance) {
		List<Method> ml = new ArrayList<Method>();
		for (Method m : instance.getClass().getMethods()){
			ml.add(m);
		}
		return ml;
	}
}
