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

import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.sequences.ListObject;
import me.enerccio.sp.types.sequences.SequenceObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

/**
 * list()
 * @author Enerccio
 *
 */
public class ListTypeObject extends TypeObject {
	private static final long serialVersionUID = -4391029961115891279L;
	public static final String LIST_CALL = "list";
	public static final String MAKE_LIST_CALL = "make_list";

	@Override
	public String getTypeIdentificator() {
		return "list";
	}

	public static PythonObject make_list(TupleObject args, KwArgs kwargs) {
		ListObject lo = new ListObject();
		lo.newObject();
		for (int i=0; i<args.len(); i++)
			lo.append(args.get(i));
		return lo;
	}
	
	public static ListObject make_list(PythonObject o) {
		ListObject lo;
		if (o instanceof SequenceObject)
			lo = new ListObject((SequenceObject)o);
		 else
			lo = new ListObject(o);
		lo.newObject();
		return lo;
	}

	
	@Override
	public PythonObject call(TupleObject args, KwArgs kwargs){
		if (kwargs != null)
			kwargs.notExpectingKWArgs();	// Throws exception if there is kwarg defined
		;
		if (args.len() == 0) {
			ListObject lo = new ListObject();
			lo.newObject();
			return lo;
		} else if (args.len() == 1) {
			return make_list(args.get(0));
		}
		throw Utils.throwException("TypeError", "list() takes at most 1 argument");
	}

}
