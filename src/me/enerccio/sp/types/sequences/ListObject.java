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

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.base.SliceObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.utils.Utils;

/**
 * Python list representation.
 * @author Enerccio
 *
 */
public class ListObject extends MutableSequenceObject implements SimpleIDAccessor  {
	private static final long serialVersionUID = 16L;

	public ListObject(){
		
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
	public PythonObject createIterator() {
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