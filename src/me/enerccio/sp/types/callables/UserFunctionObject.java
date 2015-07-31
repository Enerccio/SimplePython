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
package me.enerccio.sp.types.callables;

import java.util.ArrayList;
import java.util.List;

import me.enerccio.sp.interpret.CompiledBlockObject;
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.mappings.DictObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

/**
 * Represents user function (compiled).
 * @author Enerccio
 *
 */
public class UserFunctionObject extends CallableObject {
	private static final long serialVersionUID = 22L;
	
	/** Bytecode of this function */
	public CompiledBlockObject block;
	/** Arguments this function has */
	public List<String> args;
	/** Whether this function is vararg */
	public boolean isVararg;
	/** Vararg name */
	public String vararg;
	
	public UserFunctionObject(){
		
	}
	
	@Override
	public void newObject() {
		super.newObject();
		
		try {
			Utils.putPublic(this, CallableObject.__CALL__, new JavaMethodObject(this, "call"));
		} catch (NoSuchMethodException e){
			// will not happen
		}
	};
	
	/**
	 * Calls this function. Will insert onto frame stack and returns None.
	 * @param args
	 * @return
	 */
	@Override
	public PythonObject call(TupleObject args, KwArgs kwargs) {
		args = refillArgs(args, kwargs);
		int argc = args.len();
		int rargs = this.args.size();
		if (isVararg)
			++rargs;
		
		if (argc < rargs)
			throw Utils.throwException("TypeError",  fields.get("__name__").object + "(): incorrect amount of arguments, expected at least " + rargs + ", got " + args.len());
		
		if (!isVararg && argc > rargs)
			throw Utils.throwException("TypeError", fields.get("__name__").object + "(): incorrect amount of arguments, expected at most " + rargs + ", got " + args.len());
			
		DictObject a = new DictObject();
		
		List<PythonObject> vargs = new ArrayList<PythonObject>();
		for (int i=0; i<argc; i++){
			if (i < this.args.size())
				a.put(this.args.get(i), args.getObjects()[i]);
			else
				vargs.add(args.getObjects()[i]);
		}
		
		if (isVararg){
			TupleObject t = (TupleObject) Utils.list2tuple(vargs);
			t.newObject();
			a.put(vararg, t);
		}
		
		PythonInterpret.interpret.get().setArgs(a);
		PythonInterpret.interpret.get().executeBytecode(block);
		
		return NoneObject.NONE; // returns immediately
	}

	/**
	 * Adds variables from defaults
	 * @param args
	 * @return
	 */
	public TupleObject refillArgs(TupleObject args, KwArgs kwargs) {
		DictObject m = (DictObject) fields.get("function_defaults").object;
		PythonObject[] pl = new PythonObject[this.args.size()];
		for (int i=0; i<pl.length; i++) {
			String key = this.args.get(i);
			if (i < args.len()) {
				// Argument passed in tuple
				pl[i] = args.get(i);
				if (kwargs != null && kwargs.contains(key))
					throw Utils.throwException("TypeError", fields.get("__name__").object + "() got multiple values for keyword argument '" + key + "'");
			} else if ((kwargs != null) && (kwargs.contains(key))) {
				// Argument passed in kwargs
				pl[i] = kwargs.consume(key);
			} else if (m.contains(key)) {
				// Argument filled from defaults
				pl[i] = m.getItem(key);
			} else {
				// Missing argument
				throw Utils.throwException("TypeError", fields.get("__name__").object + "() required argument '" + key + "' missing");
			}
		}
		if (kwargs != null)
			kwargs.checkEmpty(fields.get("__name__").object + "()");
		return new TupleObject(pl); // pl.toArray(new PythonObject[pl.size()]));
	}

	@Override
	protected String doToString() {
		return "<function " + fields.get("__name__").object + ">";
	}

	@Override
	public boolean truthValue() {
		return true;
	}
}
