/*
 * SimplePython - embeddable python interpret in java
 * Copyright (c) Peter Vanusanik, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package me.enerccio.sp.types.pointer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.enerccio.sp.types.callables.JavaCongruentAggregatorObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.properties.FieldPropertyObject;
import me.enerccio.sp.utils.Pair;
import me.enerccio.sp.utils.Utils;

/**
 * Base wrapping factory. Handles wrapping methods into JavaCongruentAggregatorObjects. 
 * Which methods to wrap are decided by the getMethods method.
 * @author Enerccio
 *
 */
public abstract class WrapBaseFactory implements PointerFactory {
	private static final long serialVersionUID = -4111009373007823950L;
	
	private static Map<String, List<Method>> cache = Collections.synchronizedMap(new HashMap<String, List<Method>>());
	private static Map<String, List<Pair<Field, Boolean>>> fcache = Collections.synchronizedMap(new HashMap<String, List<Pair<Field, Boolean>>>());

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
		
		if (!fcache.containsKey(instance.getClass().getCanonicalName()))
			synchronized (fcache){
				if (!fcache.containsKey(instance.getClass().getCanonicalName())){
					List<Pair<Field, Boolean>> ml = getFields(instance);
					fcache.put(instance.getClass().getCanonicalName(), ml);
				}
			}
		
		synchronized (cache){
			Map<String, JavaCongruentAggregatorObject> mm = new HashMap<String, JavaCongruentAggregatorObject>();
			
			for (Method m : cache.get(instance.getClass().getCanonicalName())){
				String name = m.getName();
				if (!mm.containsKey(name)){
					JavaCongruentAggregatorObject co = new JavaCongruentAggregatorObject(name);
					mm.put(name, co);
				}
				mm.get(name).methods.add(new JavaMethodObject(instance, m));
			}
			
			for (String name : mm.keySet()){
				Utils.putPublic(o, name, mm.get(name));
			}
			
			for (Pair<Field, Boolean> fd : fcache.get(instance.getClass().getCanonicalName())){
				Utils.putPublic(o, fd.getFirst().getName(), new FieldPropertyObject(instance, fd.getFirst(), fd.getSecond()));
			}
		}
		
		return o;
	}

	protected abstract List<Method> getMethods(Object instance);
	protected abstract List<Pair<Field, Boolean>> getFields(Object instance);
}
