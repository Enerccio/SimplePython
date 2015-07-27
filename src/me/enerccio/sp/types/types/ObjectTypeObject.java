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

import java.util.ArrayList;

import me.enerccio.sp.compiler.Bytecode;
import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.types.base.ClassInstanceObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.callables.UserFunctionObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class ObjectTypeObject extends TypeObject {
	private static final long serialVersionUID = 4583318830595686027L;
	public static final String OBJECT_CALL = "object";
	public static final String __CONTAINS__ = "__contains__";
	public static final String IS = "is";
	
	public static final ObjectTypeObject inst = new ObjectTypeObject();
	
	@Override
	public void newObject() {
		super.newObject();
		Utils.putPublic(this, "__name__", new StringObject("object"));
		Utils.putPublic(this, "__bases__", new TupleObject());
		MapObject md = null;
		Utils.putPublic(this, "__dict__", md = new MapObject());
		
		UserFunctionObject usf = new UserFunctionObject();
		usf.newObject();
		Utils.putPublic(usf, "__name__", new StringObject("object.__init__"));
		usf.args = new ArrayList<String>();
		usf.args.add("self");
		Utils.putPublic(usf, "function_defaults", new MapObject());
		PythonBytecode cb;
		usf.bytecode.add(Bytecode.makeBytecode(Bytecode.PUSH_ENVIRONMENT));
		usf.bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH));
		cb.value = NoneObject.NONE;
		usf.bytecode.add(cb = Bytecode.makeBytecode(Bytecode.RETURN));
		cb.intValue = 1;
		
		md.put(ClassInstanceObject.__INIT__, usf);
	}

	@Override
	public String getTypeIdentificator() {
		return "object";
	}

}
