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
package me.enerccio.sp.external;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.types.pointer.WrapAnnotationFactory.WrapMethod;

public class PythonMutex {

	private ReentrantLock m;

	public PythonMutex() {
		m = new ReentrantLock();
	}

	@WrapMethod
	public void acquire() {
		m.lock();
	}

	@WrapMethod
	public void try_acquire() {
		m.tryLock();
	}

	@WrapMethod
	public void try_acquire_timeout(long ms, String unit) {
		try {
			m.tryLock(ms, TimeUnit.valueOf(unit));
		} catch (InterruptedException e) {
			throw new TypeError("jmutext(): interrupted");
		} catch (IllegalArgumentException e) {
			throw new TypeError("jmuted(): wrong unit type '" + unit + "'");
		}
	}

	@WrapMethod
	public void release() {
		m.unlock();
	}
}
