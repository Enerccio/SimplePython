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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.utils.Utils;

/**
 * Sequence iterator for system classes list and tuple
 * @author Enerccio
 *
 */
public class OrderedSequenceIterator extends PythonObject {
	private static final long serialVersionUID = 4746975236443204424L;
	public static final String NEXT = "next";
	private SequenceObject sequence;
	private int cp = 0;
	private int len = 0;

	public OrderedSequenceIterator(SequenceObject sequenceObject) {
		this.sequence = sequenceObject;
		this.len = sequence.len();
	}
	
	private static Map<String, AugumentedPythonObject> sfields = Collections.synchronizedMap(new HashMap<String, AugumentedPythonObject>());
	
	static {
		try {
			Utils.putPublic(sfields, SequenceObject.__ITER__, new JavaMethodObject(null, OrderedSequenceIterator.class.getMethod("__iter__", 
					new Class<?>[]{TupleObject.class, KwArgs.class}), true));
			Utils.putPublic(sfields, NEXT, new JavaMethodObject(null, OrderedSequenceIterator.class.getMethod("next", 
					new Class<?>[]{TupleObject.class, KwArgs.class}), true));
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void newObject() {
		super.newObject();
		
		String m;
		
		m = SequenceObject.__ITER__;
		fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
				AccessRestrictions.PUBLIC));
		m = NEXT;
		fields.put(m, new AugumentedPythonObject(((JavaMethodObject)sfields.get(m).object).cloneWithThis(this), 
				AccessRestrictions.PUBLIC));
	}
	
	public PythonObject __iter__(TupleObject args, KwArgs kwargs){
		if (kwargs != null) kwargs.checkEmpty("__iter__");
		if (args.len() > 0)
			throw Utils.throwException("TypeError", "__iter__(): method requires no arguments");
		return this;
	}
	
	public PythonObject next(TupleObject args, KwArgs kwargs){
		if (kwargs != null) kwargs.checkEmpty("__iter__");
		if (args.len() > 0)
			throw Utils.throwException("TypeError", "next(): method requires no arguments");
		if (cp >= len)
			throw Utils.throwException("StopIteration");
		PythonObject value = PythonInterpret.interpret.get().execute(false, Utils.get(sequence, SequenceObject.__GETITEM__), null, IntObject.valueOf(cp++));
		return value;
	}

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	protected String doToString() {
		return "<iterator of " + sequence.toString()  + ">";
	}

}
