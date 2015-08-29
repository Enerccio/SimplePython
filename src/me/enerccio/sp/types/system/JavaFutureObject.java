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
package me.enerccio.sp.types.system;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.JavaMethodObject;

public class JavaFutureObject extends PythonObject implements FutureObject {
	private static final long serialVersionUID = 1546042922151289657L;

	private volatile FutureStatus status = FutureStatus.RUNNING;
	private Semaphore monitor = new Semaphore(0);
	private PythonObject value = null;

	public JavaFutureObject() {
		super(false);
	}

	@Override
	public PythonObject getValue() {
		
		synchronized (this) {
			if (isReady())
				return value;
		}

		try {
			monitor.tryAcquire(5, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.interrupted();
			return null;
		}
		
		return null;
	}

	public synchronized void setValue(PythonObject value) {
		if (this.value != null)
			throw new RuntimeException("JavaFuture already has a value");
		this.value = value;
		this.status = FutureStatus.FINISHED;
		monitor.release();
	}

	@Override
	public Set<String> getGenHandleNames() {
		return PythonObject.getSFields().keySet();
	}

	@Override
	protected Map<String, JavaMethodObject> getGenHandles() {
		return PythonObject.getSFields();
	}

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	protected String doToString() {
		return "<java future object>";
	}

	@Override
	public boolean isReady() {
		return status != FutureStatus.RUNNING;
	}
}
