package me.enerccio.sp.types.pointer;

import java.lang.reflect.Method;

public class WrapPublicFactory extends WrapBaseFactory implements PointerFactory {
	private static final long serialVersionUID = 693487950048251692L;

	@Override
	public PointerObject doInitialize(Object instance) {
		PointerObject o = new PointerObject(instance);
		for (Method m : instance.getClass().getMethods()){
			wrapMethod(m, o);
		}
		return o;
	}

}
