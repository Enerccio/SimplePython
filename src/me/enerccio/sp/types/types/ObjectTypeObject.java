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
import me.enerccio.sp.errors.AttributeError;
import me.enerccio.sp.interpret.CompiledBlockObject;
import me.enerccio.sp.interpret.ModuleResolver;
import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.ModuleObject.ModuleData;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.ClassInstanceObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.callables.BoundHandleObject;
import me.enerccio.sp.types.callables.JavaFunctionObject;
import me.enerccio.sp.types.callables.UserFunctionObject;
import me.enerccio.sp.types.mappings.StringDictObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

/**
 * object()
 * 
 * @author Enerccio
 *
 */
public class ObjectTypeObject extends TypeObject {
	private static final long serialVersionUID = 4583318830595686027L;
	private static final ModuleData OBJECT_MODULE_INFO = new ModuleData() {
		@Override
		public String getName() {
			return "<object>";
		}

		@Override
		public String getFileName() {
			return getName();
		}

		@Override
		public ModuleResolver getResolver() {
			return null;
		}

		@Override
		public String getPackageResolve() {
			return "";
		}

		@Override
		public boolean isPackage() {
			return false;
		}

		@Override
		public boolean isJavaClass() {
			return false;
		}
	};
	public static final String OBJECT_CALL = "object";
	public static final String __CONTAINS__ = "__contains__";
	public static final String IS = "is";

	public static final ObjectTypeObject inst = new ObjectTypeObject();

	@Override
	public void newObject() {
		if (PythonRuntime.NONE_TYPE != null) {
			super.newObject();
			Utils.putPublic(this, "__name__", new StringObject("object"));
			Utils.putPublic(this, "__bases__", new TupleObject());
			StringDictObject md = null;
			Utils.putPublic(this, "__dict__", md = new StringDictObject());

			UserFunctionObject usf = new UserFunctionObject();
			Utils.putPublic(usf, "__name__",
					new StringObject("object.__init__"));
			usf.args = new ArrayList<String>();
			usf.args.add("self");
			Utils.putPublic(usf, "function_defaults", new StringDictObject());
			PythonBytecode cb;
			List<PythonBytecode> usfb = new ArrayList<PythonBytecode>();
			usfb.add(Bytecode.makeBytecode(Bytecode.PUSH_ENVIRONMENT, null,
					null, OBJECT_MODULE_INFO));
			usfb.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, null, null,
					OBJECT_MODULE_INFO));
			cb.value = NoneObject.NONE;
			usfb.add(cb = Bytecode.makeBytecode(Bytecode.RETURN, null, null,
					OBJECT_MODULE_INFO));
			cb.intValue = 1;

			usf.block = new CompiledBlockObject(usfb);

			BoundHandleObject bh = new BoundHandleObject();
			Utils.putPublic(bh, BoundHandleObject.ACCESSOR, this);
			Utils.putPublic(bh, BoundHandleObject.FUNC, usf);
			md.put(ClassInstanceObject.__INIT__, bh);

			try {
				JavaFunctionObject func = null;
				func = new JavaFunctionObject(ObjectTypeObject.class.getMethod(
						"getattribute", new Class<?>[] { PythonObject.class,
								String.class }), false);
				func.setWrappedMethod(true);
				md.put("__getattribute__", func);
				func = new JavaFunctionObject(ObjectTypeObject.class.getMethod(
						"str", new Class<?>[] { PythonObject.class }), false);
				func.setWrappedMethod(true);
				md.put("__str__", func);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public String getTypeIdentificator() {
		return "object";
	}

	public static PythonObject getattribute(PythonObject self, String attribute) {
		PythonObject value = self.get(attribute, PythonInterpreter.interpreter
				.get().getLocalContext());
		if (value == null)
			throw new AttributeError(String.format(
					"%s object has no attribute '%s'", self, attribute));
		return value;
	}

	public static PythonObject str(PythonObject o) {
		return new StringObject(o.toString());
	}
}
