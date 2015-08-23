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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import me.enerccio.sp.errors.PythonException;
import me.enerccio.sp.interpret.EnvironmentObject;
import me.enerccio.sp.interpret.InternalDict;
import me.enerccio.sp.interpret.PythonExecutionException;
import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.callables.UserFunctionObject;
import me.enerccio.sp.types.mappings.StringDictObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class PythonFutureObject extends PythonObject implements FutureObject {
	private static final long serialVersionUID = 1546042922151289657L;

	private UserFunctionObject futureCall;
	private PythonObject result;
	private PythonObject exception;
	private InternalDict closure;
	private volatile FutureStatus status = FutureStatus.RUNNING;
	private Semaphore monitor = new Semaphore(0);

	public PythonFutureObject(UserFunctionObject fc, List<String> closureCopy,
			EnvironmentObject environment) {
		super(false);
		futureCall = fc;
		closure = new StringDictObject();
		for (String key : closureCopy) {
			PythonObject var = environment.get(key, false, false);
			if (var.get("__onfuture__", null) != null)
				var = PythonInterpreter.interpreter.get().execute(true,
						var.get("__onfuture__", null), null);
			closure.putVariable(key, var);
		}
		startNewFuture();
	}

	@Override
	public PythonObject getValue() {
		if (isReady())
			return doGetValue();

		while (true) {
			if (isReady())
				break;
			try {
				monitor.tryAcquire(5, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return null;
			}
		}

		return doGetValue();
	}

	private void startNewFuture() {
		Thread t = new Thread("future-call-thread") {
			@Override
			public void run() {
				futureCall.call(new TupleObject(true), null);
				PythonInterpreter.interpreter.get().getCurrentClosure()
						.add(0, closure);
				try {
					result = PythonInterpreter.interpreter.get().executeAll(0);
					status = FutureStatus.FINISHED;
				} catch (PythonExecutionException e) {
					exception = e.getException();
					status = FutureStatus.FAILED;
				} catch (PythonException e) {
					exception = ((PythonExecutionException) Utils
							.throwException(PythonRuntime.ERROR,
									"failed future call", e)).getException();
					status = FutureStatus.FAILED;
				}
				monitor.release();
			}
		};
		t.start();
	}

	private PythonObject doGetValue() {
		if (status == FutureStatus.FINISHED)
			return result;
		else
			throw new PythonExecutionException(exception);
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
		return "<future-call of " + futureCall.toString() + ", state = "
				+ status.toString() + ">";
	}

	@Override
	public boolean isReady() {
		return status != FutureStatus.RUNNING;
	}
}
