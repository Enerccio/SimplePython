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

import me.enerccio.sp.types.pointer.WrapAnnotationFactory.WrapMethod;

public class ThreadInfo extends Thread implements Externalizable {

	public ThreadInfo() {

	}

	@WrapMethod
	public PythonThread current_thread() {
		return new PythonThread(Thread.currentThread());
	}

	@WrapMethod
	public void wait_time(long millis, int nanos) {
		try {
			Thread.sleep(millis, nanos);
		} catch (InterruptedException e) {

		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {

	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {

	}
}
