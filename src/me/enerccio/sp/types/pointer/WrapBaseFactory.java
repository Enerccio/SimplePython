package me.enerccio.sp.types.pointer;

import java.lang.reflect.Method;

import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.utils.Utils;

public abstract class WrapBaseFactory implements PointerFactory {
	private static final long serialVersionUID = -4111009373007823950L;

	protected void wrapMethod(Method m, PointerObject o){
		Utils.putPublic(o, m.getName(), new JavaMethodObject(o.getObject(), m, false));
	}
}
