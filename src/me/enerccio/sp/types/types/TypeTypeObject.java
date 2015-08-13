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
package me.enerccio.sp.types.types;

import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.interpret.InternalDict;
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.BoundHandleObject;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.callables.UserFunctionObject;
import me.enerccio.sp.types.mappings.DictObject;
import me.enerccio.sp.types.mappings.PythonProxy;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

/**
 * type()
 * @author Enerccio
 *
 */
public class TypeTypeObject extends TypeObject {
	private static final long serialVersionUID = -9154234544871833082L;
	public static final String TYPE_CALL = "type";
	
	@Override
	public String getTypeIdentificator() {
		return "type";
	}
	
	@Override
	public ClassObject getType() {
		return this;
	}

	@Override
	public PythonObject call(TupleObject args, KwArgs kwargs){
		if (kwargs != null)
			kwargs.notExpectingKWArgs();	// Throws exception if there is kwarg defined 
		if (args.len() == 1)
			return PythonRuntime.getType(args.getObjects()[0]);
		else if (args.len() == 3)
			return newClassType(args.getObjects()[0], args.getObjects()[1], args.getObjects()[2]);
		
		throw new TypeError(" type(): incorrect number of parameters");
	}

	private PythonObject newClassType(PythonObject name,
			PythonObject bases, PythonObject dict) {
		if (!(name instanceof StringObject))
			throw new TypeError("type(): name must be a string");
		if (!(bases instanceof TupleObject))
			throw new TypeError("type(): bases must be a tuple");
		if (!(dict instanceof InternalDict))
			throw new TypeError("type(): dict must be a dict");

		ClassObject type = new ClassObject();
		type.newObject(); // TODO
		Utils.putPublic(type, ClassObject.__NAME__, name);
		Utils.putPublic(type, ClassObject.__BASES__, bases);
		Utils.putPublic(type, ClassObject.__DICT__, dict);
		
		synchronized (dict){
			InternalDict d = (InternalDict)dict;
			synchronized (d){
				for (String key : d.keySet()){
					PythonObject o = d.getVariable(key);
					if (o instanceof UserFunctionObject){
						BoundHandleObject bh = new BoundHandleObject();
						bh.newObject();
						Utils.putPublic(bh, BoundHandleObject.ACCESSOR, type);
						Utils.putPublic(bh, BoundHandleObject.FUNC, o);
						d.putVariable(key, bh);
					}
				}
			}
		}
		
		return type;
	}
}
