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
import java.util.List;

import me.enerccio.sp.compiler.Bytecode;
import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.interpret.CompiledBlockObject;
import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.ClassInstanceObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.callables.JavaFunctionObject;
import me.enerccio.sp.types.callables.UserFunctionObject;
import me.enerccio.sp.types.mappings.DictObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

/**
 * object()
 * @author Enerccio
 *
 */
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
		DictObject md = null;
		Utils.putPublic(this, "__dict__", md = new DictObject());
		
		UserFunctionObject usf = new UserFunctionObject();
		usf.newObject();
		Utils.putPublic(usf, "__name__", new StringObject("object.__init__"));
		usf.args = new ArrayList<String>();
		usf.args.add("self");
		Utils.putPublic(usf, "function_defaults", new DictObject());
		PythonBytecode cb;
		List<PythonBytecode> usfb = new ArrayList<PythonBytecode>();
		usfb.add(Bytecode.makeBytecode(Bytecode.PUSH_ENVIRONMENT));
		usfb.add(cb = Bytecode.makeBytecode(Bytecode.PUSH));
		cb.value = NoneObject.NONE;
		usfb.add(cb = Bytecode.makeBytecode(Bytecode.RETURN));
		cb.intValue = 1;
		
		usf.block = new CompiledBlockObject(usfb);
		usf.block.newObject();
		
		md.put(ClassInstanceObject.__INIT__, usf);
		try {
			JavaFunctionObject func = null;
			func = new JavaFunctionObject(ObjectTypeObject.class.getMethod("getattribute", new Class<?>[]{PythonObject.class, String.class}), false);
			func.setWrappedMethod(true);
			md.put("__getattribute__", func);
			func = new JavaFunctionObject(ObjectTypeObject.class.getMethod("str", new Class<?>[]{PythonObject.class}), false);
			func.setWrappedMethod(true);
			md.put("__str__", func);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getTypeIdentificator() {
		return "object";
	}

	
	public static PythonObject getattribute(PythonObject self, String attribute){
		PythonObject value = self.get(attribute, PythonInterpret.interpret.get().getLocalContext());
		if (value == null)
			throw Utils.throwException("AttributeError", String.format("%s object has no attribute '%s'", self, attribute));
		return value;
	}
	
	public static PythonObject str(PythonObject o){
		return new StringObject(o.toString());
	}
}
