package me.enerccio.sp.types.pointer;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.utils.Utils;

public abstract class WrapBaseFactory implements PointerFactory {
	private static final long serialVersionUID = -4111009373007823950L;

	protected void wrapMethod(Method m, PointerObject o){
		Utils.putPublic(o, m.getName(), new JavaMethodObject(o.getObject(), m, false));
	}
	
	private static Map<String, List<Method>> cache = Collections.synchronizedMap(new HashMap<String, List<Method>>());

	@Override
	public final PointerObject doInitialize(Object instance) {
		PointerObject o = new PointerObject(instance);
		
		if (!cache.containsKey(instance.getClass().getCanonicalName()))
			synchronized (cache){
				if (!cache.containsKey(instance.getClass().getCanonicalName())){
					List<Method> ml = getMethods(instance);
					cache.put(instance.getClass().getCanonicalName(), ml);
				}
			}
		
		synchronized (cache){
			for (Method m : cache.get(instance.getClass().getCanonicalName())){
				wrapMethod(m, o);
			}
		}
		
		return o;
	}

	protected abstract List<Method> getMethods(Object instance);
}
