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

import java.util.HashMap;
import java.util.Map;

import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.interpret.CompiledBlockObject;
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.sandbox.SecureAction;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.Tags;
import me.enerccio.sp.types.base.NumberObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.CastFailedException;
import me.enerccio.sp.utils.Coerce;

public class CompiledBlockTypeObject extends TypeObject {
	private static final long serialVersionUID = -6361635864292501735L;
	public static final String COMPILED_CALL = "compiled_block";

	@Override
	public String getTypeIdentificator() {
		return "compiled-block";
	}

	@Override
	public PythonObject call(TupleObject o, KwArgs varArgs) {
		if (varArgs != null)
			varArgs.checkEmpty("compiled_block");
		if (o.len() != 2)
			throw new TypeError("compiled_block(): expected 2 arguments, got "
					+ o.len());
		try {
			String s = Coerce.toJava(o.get(0), String.class);
			@SuppressWarnings("unchecked")
			Map<PythonObject, PythonObject> mm = Coerce.toJava(o.get(1),
					Map.class);
			Map<Integer, PythonObject> consts = new HashMap<Integer, PythonObject>();
			for (PythonObject key : mm.keySet()) {
				if (!NumberObject.isInteger(key))
					throw new CastFailedException("blah");
				consts.put(((NumberObject) key).intValue(), mm.get(key));
			}
			PythonRuntime.runtime.checkSandboxAction("compiled_block",
					SecureAction.RUNTIME_COMPILE, s, consts);
			CompiledBlockObject co = new CompiledBlockObject(s.getBytes(),
					consts);
			co.finishCB();
			return co;
		} catch (CastFailedException e) {
			throw new TypeError(
					"compiled_block(): first argument must be 'str' object and second argument must be 'dict' object containing 'str'->object pairs");
		}
	}

	@Override
	public byte getTag() {
		return Tags.CB_TYPE;
	}
}
