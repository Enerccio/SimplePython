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

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RebindableThreadLocal<T> {
	
	private static class WeakProxy<Y> {
		boolean reclaimed;
		Y value;
		WeakReference<Y> weakRef;
		
		synchronized Y get(){
			if (!reclaimed)
				return value;
			return weakRef.get();
		}
	}
	
	private Map<Thread, WeakProxy<T>> values = new ConcurrentHashMap<Thread, WeakProxy<T>>();
	
	public RebindableThreadLocal(){
		bind(this);
	}
	
	private static Thread cleanupDaemon;
	static {
		cleanupDaemon = new Thread(new Runnable(){

			@Override
			public void run() {
				cleanup();
				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {
					return;
				}
			}
			
		});
		cleanupDaemon.setPriority(Thread.MIN_PRIORITY);
		cleanupDaemon.setDaemon(true);
	}
	
	private static Set<RebindableThreadLocal<?>> variables = Collections.synchronizedSet(new HashSet<RebindableThreadLocal<?>>());
	private static void bind(RebindableThreadLocal<?> rebindableThreadLocal) {
		variables.add(rebindableThreadLocal);
	}

	private static void cleanup() {
		synchronized (variables){
			for (RebindableThreadLocal<?> tl : variables){
				tl.cleanDeadThreads();
			}
		}
	}

	public T get(){
		Thread t = Thread.currentThread();
		return getForThread(t);
	}
	
	private T getForThread(Thread t) {
		if (!values.containsKey(t)){
			WeakProxy<T> wp = new WeakProxy<T>();
			values.put(t, wp);
			T v = initialValue();
			wp.value = v;
			wp.reclaimed = false;
			wp.weakRef = new WeakReference<T>(v);
		}
		return values.get(t).get();
	}

	public void set(T value){
		Thread t = Thread.currentThread();
		setForThread(t, value);
	}
	
	public void setForThread(Thread t, T value){
		if (!values.containsKey(t)){
			WeakProxy<T> wp = new WeakProxy<T>();
			values.put(t, wp);
			T v = initialValue();
			wp.value = v;
			wp.reclaimed = false;
			wp.weakRef = new WeakReference<T>(v);
		}
		WeakProxy<T> wp = new WeakProxy<T>();
		values.put(t, wp);
		wp.value = value;
		wp.reclaimed = false;
		wp.weakRef = new WeakReference<T>(value);
	}

	protected T initialValue() {
		return null;
	}

	private void cleanDeadThreads() {
		for (Thread t : values.keySet()){
			if (!t.isAlive()){
				WeakProxy<T> wp = values.get(t);
				synchronized (wp){
					wp.value = null;
					wp.reclaimed = true;	
				}
			}
		}
	}
}
