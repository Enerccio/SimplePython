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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import me.enerccio.sp.SimplePython;
import me.enerccio.sp.interpret.AbstractPythonInterpreter;
import me.enerccio.sp.interpret.InternalDict;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.sandbox.SecureAction;
import me.enerccio.sp.serialization.PySerializer;
import me.enerccio.sp.types.base.ClassInstanceObject;
import me.enerccio.sp.types.callables.CallableObject;
import me.enerccio.sp.types.callables.UserMethodObject;
import me.enerccio.sp.types.pointer.WrapAnnotationFactory.WrapField;
import me.enerccio.sp.types.pointer.WrapAnnotationFactory.WrapMethod;
import me.enerccio.sp.types.sequences.TupleObject;

public class PythonThread implements Runnable, Externalizable {

	private UserMethodObject call;
	private Thread t;

	@WrapField(readOnly = true)
	public boolean executed;

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		PySerializer s = PythonRuntime.activeSerializer;
		s.serialize(call);
		s.serialize(executed);
		s.serialize(t.isAlive());
		s.serialize(t.isDaemon());
		s.serialize(t.getName());
		s.serialize(AbstractPythonInterpreter.interpreter.getForThread(t));
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {

	}

	public PythonThread(ClassInstanceObject o, String name) {
		PythonRuntime.runtime.checkSandboxAction("jthread",
				SecureAction.NEW_THREAD);

		t = new Thread(this);
		setThreadName(name);
		call = (UserMethodObject) SimplePython.getField(o, "execute");
	}

	PythonThread(Thread t) {
		this.t = t;
	}

	@Override
	public void run() {
		executed = true;

		if (call != null)
			AbstractPythonInterpreter.interpreter.get().execute(true, call,
					null);
	};

	@WrapMethod
	public void setDaemon(boolean daemon) {
		t.setDaemon(daemon);
	}

	@WrapMethod
	public void threadStart() {
		t.start();
	}

	@WrapMethod
	public boolean threadRunning() {
		return t.isAlive();
	}

	@WrapMethod
	public void setThreadName(String name) {
		if (name != null)
			t.setName(name);
	}

	@WrapMethod
	public String getThreadName() {
		return t.getName();
	}

	@WrapMethod
	public void waitJoin() {
		try {
			t.join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	@WrapMethod
	public void interruptThread() {
		t.interrupt();
	}

	@WrapMethod
	public void signal(CallableObject o, TupleObject args, InternalDict dict) {
		AbstractPythonInterpreter.interpreterMap.get(t).interruptInterpret(o,
				args, dict.asKwargs());
	}
}
