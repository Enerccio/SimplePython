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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import me.enerccio.sp.utils.Pair;

/**
 * Wraps all public methods wrapped by @WrapMethod
 * 
 * @author Enerccio
 *
 */
public class WrapAnnotationFactory extends WrapBaseFactory {
	private static final long serialVersionUID = -5142774589035715501L;

	@Retention(RetentionPolicy.RUNTIME)
	public static @interface WrapMethod {
	}

	@Retention(RetentionPolicy.RUNTIME)
	public static @interface WrapField {
		boolean readOnly() default false;
	}

	@Override
	protected List<Method> getMethods(Class<?> clazz) {
		List<Method> ml = new ArrayList<Method>();
		for (Method m : clazz.getMethods()) {
			if (m.isAnnotationPresent(WrapMethod.class))
				ml.add(m);
		}
		return ml;
	}

	@Override
	protected List<Pair<Field, Boolean>> getFields(Class<?> clazz) {
		List<Pair<Field, Boolean>> fl = new ArrayList<Pair<Field, Boolean>>();
		for (Field f : clazz.getFields())
			if (f.isAnnotationPresent(WrapField.class))
				fl.add(Pair.makePair(f, f.getAnnotation(WrapField.class)
						.readOnly()));
		return fl;
	}

}
