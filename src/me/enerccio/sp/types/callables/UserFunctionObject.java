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
import java.util.Arrays;
import java.util.List;

import me.enerccio.sp.interpret.CompiledBlockObject;
import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

/**
 * Represents user function (compiled).
 * @author Enerccio
 *
 */
public class UserFunctionObject extends PythonObject {
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
			Utils.putPublic(this, CallableObject.__CALL__, new JavaMethodObject(this, this.getClass().getMethod("call", 
					new Class<?>[]{TupleObject.class}), true));
		} catch (NoSuchMethodException e){
			// will not happen
		}
	};

	/**
	 * Calls this function. Will insert onto frame stack and returns None.
	 * @param args
	 * @return
	 */
	public PythonObject call(TupleObject args) {
		args = refillArgs(args);
		int argc = args.len();
		int rargs = this.args.size();
		if (isVararg)
			++rargs;
		
		if (argc < rargs)
			throw Utils.throwException("TypeError",  fields.get("__name__").object + "(): incorrect amount of arguments, expected at least " + rargs + ", got " + args.len());
		
		if (!isVararg && argc > rargs)
			throw Utils.throwException("TypeError", fields.get("__name__").object + "(): incorrect amount of arguments, expected at most " + rargs + ", got " + args.len());
			
		MapObject a = new MapObject();
		
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
	private TupleObject refillArgs(TupleObject args) {
		MapObject m = (MapObject) fields.get("function_defaults").object;
		List<PythonObject> pl = new ArrayList<PythonObject>(Arrays.asList(args.getObjects()));
		
		if (args.len() < this.args.size()){
			for (int i=args.len(); i<this.args.size(); i++){
				if (!m.contains(this.args.get(i)))
					throw Utils.throwException("TypeError", fields.get("__name__").object + "(): argument '" + this.args.get(i) + "' not specified");
				pl.add(m.backingMap.get(new StringObject(this.args.get(i))));
			}
		}
		
		return new TupleObject(pl.toArray(new PythonObject[pl.size()]));
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
