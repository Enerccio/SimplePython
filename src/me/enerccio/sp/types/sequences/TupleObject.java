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

import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.interpret.PythonExecutionException;
import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.NumberObject;
import me.enerccio.sp.types.base.SliceObject;
import me.enerccio.sp.types.iterators.InternalIterator;
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
	public static final TupleObject EMPTY = new TupleObject(false);
	
	private TupleObject(){
		super(false);
		array = new PythonObject[0];
	}
	
	private TupleObject(boolean internalUse){
		super(internalUse);
		array = new PythonObject[0];
	}
	
	public TupleObject(PythonObject... args){
		super(false);
		array = args;
	}

	public TupleObject(boolean internalUse, PythonObject... args) {
		super(internalUse);
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
		throw new TypeError("function takes no arguments");
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
		throw new TypeError("function takes no arguments");
	}
	
	@Override
	public int getId(){
		final int prime = 31;
        int result = 1;
        for (int i=0; i<array.length; i++)
	        result = (prime * result
	                + ((NumberObject)Utils.run("hash", array[i])).intValue());

        return result;
	}

	/**
	 * returns objects contained in this tuple object
	 * @return
	 */
	public PythonObject[] getObjects() {
		return array;
	}
	
	public PythonObject add(PythonObject b) {
		if (b instanceof TupleObject) {
			PythonObject[] ar = new PythonObject[len() + ((TupleObject)b).len()];
			int i = 0;
			for (PythonObject o : array)
				ar[i++] = o;
			for (PythonObject o : ((TupleObject)b).array)
				ar[i++] = o;
			TupleObject t = new TupleObject(ar);
			return t;
		}
		throw new TypeError("can only concatenate tuple (not '" + b.toString() + "') to tuple");
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
			return doGet(this, key);
	}

	@Override
	public PythonObject __iter__() {
		return new OrderedSequenceIterator(this);
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
		throw new TypeError("'" + Utils.run("typename", this) + "' object doesn't support item deletion");
	}

	public static TupleObject fromSequence(SequenceObject o) {
		List<PythonObject> tl = new ArrayList<PythonObject>();
		makeFromSequence(o, tl);
		return (TupleObject) Utils.list2tuple(tl, false);
	}

	private static void makeFromSequence(SequenceObject o, List<PythonObject> tl) {
		for (int i = 0; i<o.len(); i++)
			tl.add(o.get(NumberObject.valueOf(i)));
	}

	public static TupleObject fromIterator(PythonObject o) {
		List<PythonObject> tl = new ArrayList<PythonObject>();
		makeFromIterator(o, tl);
		return (TupleObject) Utils.list2tuple(tl, false);
	}

	private static void makeFromIterator(PythonObject o, List<PythonObject> tl) {
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
						tl.add(item);
						item = ii.next();
					}
					return;
				}
			}
			PythonObject next = iterator.get("next", null);
			if (next == null)
				throw new TypeError("iterator of " + o.toString() + " has no next() method");
			while (true) {
				PythonObject item = PythonInterpreter.interpreter.get().execute(true, next, null);
				tl.add(item);
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
}