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
import java.util.Map.Entry;

import me.enerccio.sp.compiler.Bytecode;
import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.interpret.CompiledBlockObject;
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.mappings.DictObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

/**
 * Represents python methods
 * @author Enerccio
 *
 */
public class UserMethodObject extends CallableObject {
	private static final long serialVersionUID = 6184279154550720464L;
	public static final String SELF = "__self__";
	public static final String FUNC = "__func__";
	public static final String ACCESSOR = "__access__";
	
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
	 * Returns runtime made bytecode for method handler.
	 * @param o
	 * @param args
	 * @param kwargs 
	 * @return
	 */
	public CompiledBlockObject methodCall(UserMethodObject o, TupleObject args, KwArgs kwargs) {
		PythonBytecode b = null;
		List<PythonBytecode> l = new ArrayList<PythonBytecode>();

		PythonObject callable = o.get(UserMethodObject.SELF, o);
		PythonObject caller = o.get(UserMethodObject.FUNC, o);
		
		// []
		l.add(Bytecode.makeBytecode(Bytecode.PUSH_ENVIRONMENT));
		// environments
		if (caller instanceof UserFunctionObject && caller.fields.containsKey("closure")){
			// add closure
			for (PythonObject d : ((TupleObject) caller.fields.get("closure").object).getObjects()){
				l.add(b = Bytecode.makeBytecode(Bytecode.PUSH_DICT));
				b.mapValue = (DictObject) d;	
			}
		} else {
			// add globals
			l.add(b = Bytecode.makeBytecode(Bytecode.PUSH_DICT));
			b.mapValue = new DictObject();
			l.add(b = Bytecode.makeBytecode(Bytecode.PUSH_DICT));
			b.mapValue = PythonRuntime.runtime.generateGlobals();
		}
		
		l.add(b = Bytecode.makeBytecode(Bytecode.PUSH));
		if ( fields.get(ACCESSOR) == null)
			b.value = NoneObject.NONE;
		else
			b.value = fields.get(ACCESSOR).object;
		// [ python object __accessor__ ]
		l.add(Bytecode.makeBytecode(Bytecode.PUSH_LOCAL_CONTEXT));
		// []
		
		l.add(b = Bytecode.makeBytecode(Bytecode.PUSH));
		b.value = caller;
		// [ callable ]
		l.add(b = Bytecode.makeBytecode(Bytecode.PUSH));
		b.value = callable;
		// [ callable, python object ]
		
		for (int i=0; i<args.len(); i++){
			l.add(b = Bytecode.makeBytecode(Bytecode.PUSH));
			b.value = args.valueAt(i);
			// [ callable, python object, python object*++ ]
		}
		if (kwargs != null) {
			for (Entry<String, PythonObject> e : kwargs.entrySet()) {
				l.add(b = Bytecode.makeBytecode(Bytecode.PUSH));
				b.value = e.getValue();
				l.add(b = Bytecode.makeBytecode(Bytecode.KWARG));
				b.stringValue = e.getKey();
			}
		}
		// [ callable, python object, python object* ]
		l.add(b = Bytecode.makeBytecode(Bytecode.CALL));
		b.intValue = args.len() + 1;
		// [ python object ]
		l.add(b = Bytecode.makeBytecode(Bytecode.RETURN));
		b.intValue = 1;
		// []
		
		CompiledBlockObject cbc = new CompiledBlockObject(l);
		cbc.newObject();
		return cbc;
	}
	
	/**
	 * Calls this method. Will insert onto frame stack and returns None.
	 * @param args
	 * @return
	 */
	public PythonObject call(TupleObject args, KwArgs kwargs) {
		PythonInterpret.interpret.get().executeBytecode(methodCall(this, args, kwargs));
		return NoneObject.NONE; // returns immediately
	}

	@Override
	public PythonObject set(String key, PythonObject localContext, PythonObject value) {
		if (key.equals(SELF) || key.equals(FUNC) || key.equals(ACCESSOR))
			throw Utils.throwException("AttributeError", "'" + 
					Utils.run("str", Utils.run("type", this)) + "' object attribute '" + key + "' is read only");
		return super.set(key, localContext, value);
	}

	@Override
	protected String doToString() {
		return "<method " + fields.get(FUNC).object + " of object " + fields.get(SELF).object + ">"; // TODO
	}

	@Override
	public boolean truthValue() {
		return true;
	}
}
