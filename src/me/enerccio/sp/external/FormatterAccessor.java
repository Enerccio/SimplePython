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
package me.enerccio.sp.external;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.serialization.PySerializer;
import me.enerccio.sp.types.callables.CallableObject;
import me.enerccio.sp.types.mappings.Dictionary;
import me.enerccio.sp.types.pointer.WrapAnnotationFactory.WrapMethod;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Formatter;

public class FormatterAccessor implements Externalizable {

	private CallableObject getValue;
	private CallableObject checkUnused;
	private CallableObject formatField;
	

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		PySerializer s = PythonRuntime.activeSerializer;
		s.serialize(getValue);
		s.serialize(checkUnused);
		s.serialize(formatField);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		// TODO Auto-generated method stub
		
	}

	public FormatterAccessor(CallableObject getValue,
			CallableObject checkUnused, CallableObject formatField) {
		this.getValue = getValue;
		this.checkUnused = checkUnused;
		this.formatField = formatField;
	}

	@WrapMethod
	public String doFormat(String formatString, TupleObject to, Dictionary dict) {
		return new Formatter(to, dict, getValue, checkUnused, formatField)
				.format(formatString).doConsume();
	}
}
