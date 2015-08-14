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

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.types.pointer.WrapAnnotationFactory.WrapMethod;

public class PythonMutex {
	
	private Semaphore m;
	private Thread currentHolder = null;
	int ac;
	
	public PythonMutex(){
		m = new Semaphore(1);
	}
	
	@WrapMethod
	public void acquire(){
		try {
			m.acquire();
		} catch (InterruptedException e) {
			throw new TypeError("jmutext(): interrupted");
		}
		assignThread();
	}
	
	private void assignThread() {
		Thread t = Thread.currentThread();
		if (currentHolder == null || !t.equals(currentHolder)){
			ac = 0;
			currentHolder = t;
		} else
			++ac;
	}

	@WrapMethod
	public void try_acquire(){
		if (m.tryAcquire()){
			assignThread();
		}
	}
	
	@WrapMethod
	public void try_acquire_timeout(long ms, String unit){
		try {
			if (m.tryAcquire(ms, TimeUnit.valueOf(unit)))
				assignThread();
		} catch (InterruptedException e) {
			throw new TypeError("jmutext(): interrupted");
		} catch (IllegalArgumentException e){
			throw new TypeError("jmuted(): wrong unit type '" + unit + "'");
		}
	}
	
	@WrapMethod
	public void release(){
		Thread t = Thread.currentThread();
		if (currentHolder == null || !t.equals(currentHolder))
			throw new TypeError("release(): mutex can only be released by the thread it was acquired");
		if (ac == 0) {
			currentHolder = null;
			m.release();
		} else
			--ac;
	}
}
