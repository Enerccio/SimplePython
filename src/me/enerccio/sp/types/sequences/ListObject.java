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
package me.enerccio.sp.types.sequences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.enerccio.sp.interpret.PythonExecutionException;
import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.base.SliceObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.iterators.InternalIterator;
import me.enerccio.sp.types.iterators.InternallyIterable;
import me.enerccio.sp.types.iterators.OrderedSequenceIterator;
import me.enerccio.sp.utils.Utils;

/**
 * Python list representation.
 * @author Enerccio
 *
 */
public class ListObject extends MutableSequenceObject implements SimpleIDAccessor, InternallyIterable  {
	private static final long serialVersionUID = 16L;

	public ListObject(){
		
	}
	
	public ListObject(SequenceObject o) {
		for (int i = 0; i<o.len(); i++)
			append(o.get(IntObject.valueOf(i)));
	}
	
	/** If passed object is iterable or has __GETITEM__ defined, creates list filled with objects in this list */ 
	public ListObject(PythonObject o) {
		PythonObject iter = o.get(__ITER__, null);
		try {
			PythonObject iterator;
			if (iter == null) {
				// Use iter() function to grab iterator
				iterator = Utils.run("iter", o);
			} else {
				iterator = PythonInterpreter.interpreter.get().execute(true, iter, null);
				if (iterator instanceof InternalIterator) {
					InternalIterator ii = (InternalIterator)iterator;
					PythonObject item = ii.next();
					while (item != null) {
						append(item);
						item = ii.next();
					}
					return;
				}
			}
			PythonObject next = iterator.get("next", null);
			if (next == null)
				throw Utils.throwException("TypeError", "iterator of " + o.toString() + " has no next() method");
			while (true) {
				PythonObject item = PythonInterpreter.interpreter.get().execute(true, next, null);
				append(item);
			}
		} catch (PythonExecutionException e) {
			if (PythonRuntime.isinstance(e.getException(), PythonRuntime.STOP_ITERATION).truthValue())
				; // nothing
			else if (PythonRuntime.isinstance(e.getException(), PythonRuntime.INDEX_ERROR).truthValue())
				; // still nothing
			else
				throw e;
		}
	}

	private static Map<String, JavaMethodObject> sfields = new HashMap<String, JavaMethodObject>();
	
	static {
		try {
			sfields.put("append", new JavaMethodObject(ListObject.class, "append", PythonObject.class));
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void newObject() {
		super.newObject();
		bindMethods(sfields);
	}
	
	public synchronized PythonObject append(PythonObject value){
		objects.add(value);
		return this;
	}
	
	public List<PythonObject> objects = Collections.synchronizedList(new ArrayList<PythonObject>());
	
	@Override
	public int len() {
		return objects.size();
	}
	
	@Override
	protected String doToString() {
		return objects.toString();
	}

	@Override
	public int hashCode(){
		return super.hashCode();
	}

	@Override
	public PythonObject get(int i) {
		if (i >= objects.size() || i<-(objects.size()))
			throw Utils.throwException("IndexError", "Incorrect index, expected (" + -objects.size() + ", " + objects.size() + "), got " + i);
		return objects.get(Utils.morphAround(i, objects.size()));
	}
	
	@Override
	public PythonObject get(PythonObject key) {
		if (key instanceof SliceObject){
			ListObject lo = new ListObject();
			lo.newObject();
			
			int[] slicedata = getSliceData(objects.size(), key);
			int sav = slicedata[0];
			int sov = slicedata[1];
			int stv = slicedata[2];
			boolean reverse = slicedata[3] == 1;
			
			synchronized (objects){
				if (sav <= sov)
					for (int i=sav; i<sov; i+=stv)
						lo.objects.add(objects.get(i));
				else
					for (int i=sov; i<sav; i+=stv)
						lo.objects.add(objects.get(i));
				if (reverse)
					synchronized (lo.objects){
						Collections.reverse(lo.objects);
					}
			}
			
			return lo;
		} else 
		return Utils.doGet(this, key);
	}

	@Override
	public PythonObject __iter__() {
		PythonObject o = new OrderedSequenceIterator(this);
		o.newObject();
		return o;
	}
	
	@Override
	public PythonObject valueAt(int idx) {
		return objects.get(idx);
	}

	@Override
	public PythonObject set(PythonObject key, PythonObject value) {
		
		if (key instanceof IntObject){
			int i = (int) ((IntObject)key).intValue();
			if (i >= len() || i<-(len()))
				throw Utils.throwException("IndexError", "incorrect index, expected (" + -len() + ", " + len() + "), got " + i);
			int idx = Utils.morphAround(i, len());
			objects.set(idx, value);
		} else if (key instanceof SliceObject){
			
		} else {
			throw Utils.throwException("TypeError", "key must be int or slice");
		}
		
		return this;
	}

	@Override
	public boolean containsItem(PythonObject o) {
		return objects.contains(o);
	}
}