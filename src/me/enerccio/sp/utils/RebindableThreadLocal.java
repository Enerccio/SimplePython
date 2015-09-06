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
package me.enerccio.sp.utils;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class RebindableThreadLocal<T> {

	private Map<Thread, T> values = Collections
			.synchronizedMap(new WeakHashMap<Thread, T>());

	public RebindableThreadLocal() {

	}

	public T get() {
		Thread t = Thread.currentThread();
		return getForThread(t);
	}

	public T getForThread(Thread t) {
		if (!values.containsKey(t))
			values.put(t, initialValue());
		return values.get(t);
	}

	public void set(T value) {
		Thread t = Thread.currentThread();
		setForThread(t, value);
	}

	public void setForThread(Thread t, T value) {
		values.put(t, value);
	}

	protected T initialValue() {
		return null;
	}
}
