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
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.sequences.ListObject;
import me.enerccio.sp.types.sequences.SequenceObject;
import me.enerccio.sp.types.sequences.TupleObject;

/**
 * tuple()
 * 
 * @author Enerccio
 *
 */
public class TupleTypeObject extends TypeObject {
	private static final long serialVersionUID = -5391029961115891279L;
	public static final String TUPLE_CALL = "tuple";
	public static final String MAKE_TUPLE_CALL = "make_tuple";

	@Override
	public String getTypeIdentificator() {
		return "tuple";
	}

	public static PythonObject make_tuple(TupleObject args, KwArgs kwargs) {
		return args;
	}

	public static TupleObject make_tuple(PythonObject o) {
		TupleObject lo;
		if (o instanceof SequenceObject)
			lo = TupleObject.fromSequence((SequenceObject) o);
		else
			lo = TupleObject.fromIterator(o);
		return lo;
	}

	@Override
	public PythonObject call(TupleObject args, KwArgs kwargs) {
		if (kwargs != null)
			kwargs.notExpectingKWArgs(); // Throws exception if there is kwarg
											// defined
		;
		if (args.len() == 0) {
			ListObject lo = new ListObject();
			return lo;
		} else if (args.len() == 1) {
			return make_tuple(args.get(0));
		}
		throw new TypeError("tuple() takes at most 1 argument");
	}

}
