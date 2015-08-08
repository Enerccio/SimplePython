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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.base.SliceObject;
import me.enerccio.sp.types.iterators.OrderedSequenceIterator;
import me.enerccio.sp.types.mappings.DictObject;
import me.enerccio.sp.utils.Utils;

/**
 * Python tuple object
 * @author Enerccio
 *
 */
public class TupleObject extends ImmutableSequenceObject  implements SimpleIDAccessor {
	private static final long serialVersionUID = 12L;
	public static final TupleObject EMPTY = new TupleObject();
	private volatile boolean initialized = false;
	
	private TupleObject(){
		array = new PythonObject[0];
	}

	public TupleObject(PythonObject... args) {
		array = args;
	}
	
	private PythonObject[] array;
	
	@Override
	public int len() {
		return array.length;
	}
	
	/** 
	 * Throws exception if there is any element in tuple. Should be used in parameters expecting function.
	 */
	public void notExpectingArgs() {
		if (array.length == 0)
			return;
		throw Utils.throwException("TypeError", "function takes no arguments");
	}

	/** 
	 * Throws exception if there is any element in tuple. Should be used in parameters expecting function.
	 * Checks passed KWargs as well, throws exception if KWargs is not null nor empty.
	 */
	public void notExpectingArgs(KwArgs kw) {
		if (kw != null)
			kw.notExpectingKWArgs();
		if (array.length == 0)
			return;
		throw Utils.throwException("TypeError", "function takes no arguments");
	}
	
	@Override
	public void newObject(){
		if (!initialized)
			synchronized (this){
				if (!initialized){
					super.newObject();
					initialized = true;
				}
			}
	}
	
	@Override
	public int getId(){
		final int prime = 31;
        int result = 1;
        for (int i=0; i<array.length; i++)
	        result = (int) (prime * result
	                + ((IntObject)Utils.run("hash", array[i])).intValue());

        return result;
	}

	/**
	 * returns objects contained in this tuple object
	 * @return
	 */
	public PythonObject[] getObjects() {
		return array;
	}

	@Override
	protected String doToString() {
		if (array.length == 0)
			return "()";
		List<PythonObject> arr = Arrays.asList(array);
		String text = arr.toString();
		return "(" + text.substring(1, text.length()-1) + ")";
	}

	/** Throws ArrayIndexOutOfBoundsException if i is out of range */
	public PythonObject get(int i) {
		return array[i]; 
	}
	
	@Override
	public PythonObject get(PythonObject key) {
		if (key instanceof SliceObject){
			TupleObject to = new TupleObject();
			to.newObject();
			
			int[] slicedata = getSliceData(array.length, key);
			int sav = slicedata[0];
			int sov = slicedata[1];
			int stv = slicedata[2];
			boolean reverse = slicedata[3] == 1;
			
			List<PythonObject> lo = new ArrayList<PythonObject>();
			
			if (sav <= sov)
				for (int i=sav; i<sov; i+=stv)
					lo.add(array[i]);
			else
				for (int i=sov; i<sav; i+=stv)
					lo.add(array[i]);
			if (reverse)
				Collections.reverse(lo);
			
			to.array = lo.toArray(new PythonObject[lo.size()]);
			
			return to;
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
		return array[idx];
	}

	@Override
	public boolean containsItem(PythonObject o) {
		for (int i=0; i<len(); i++)
			if (o.equals(array[i]))
				return true;
		return false;
	}

	public DictObject convertKwargs(KwArgs kwargs) {
		if (kwargs == null)
			return new DictObject();
		return kwargs.toDict();
	}

	@Override
	public void deleteKey(PythonObject key) {
		throw Utils.throwException("TypeError", "'" + Utils.run("typename", this) + "' object doesn't support item deletion");
	}
}