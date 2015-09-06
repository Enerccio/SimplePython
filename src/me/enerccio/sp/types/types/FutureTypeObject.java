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

import java.util.Map;
import java.util.Set;

import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.serialization.PySerializer;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.Tags;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.types.system.FutureObject;
import me.enerccio.sp.utils.Coerce;

/**
 * bool()
 * 
 * @author Enerccio
 *
 */
public class FutureTypeObject extends TypeObject {
	private static final long serialVersionUID = 6840091655061000673L;
	public static final String FUTURE_CALL = "future_object";

	@Override
	public String getTypeIdentificator() {
		return "bool";
	}

	@Override
	public byte getTag() {
		return Tags.FUTURE_TYPE;
	}

	public static class FutureQuery extends PythonObject implements
			FutureObject {
		private static final long serialVersionUID = 8916981825344941893L;
		private FutureObject inner;

		public FutureQuery(FutureObject inner) {
			super(false);
			this.inner = inner;
		}

		@Override
		public byte getTag() {
			return Tags.FUTUREQ;
		}

		@Override
		public PythonObject getValue() {
			return this;
		}

		@Override
		public boolean isReady() {
			boolean ready = inner.isReady();
			return ready;
		}

		@Override
		public Set<String> getGenHandleNames() {
			return PythonObject.sfields.keySet();
		}

		@Override
		protected Map<String, JavaMethodObject> getGenHandles() {
			return PythonObject.sfields;
		}

		@Override
		public boolean truthValue() {
			return true;
		}

		@Override
		protected String doToString() {
			return "<Future Query for " + inner.toString() + ">";
		}

		@Override
		protected void serializeDirectState(PySerializer pySerializer) {
			if (inner instanceof PythonObject)
				pySerializer.serialize((PythonObject) inner);
			else
				pySerializer.serializeJava(inner);
		}

	}

	@Override
	public PythonObject call(TupleObject args, KwArgs kwargs) {
		if (kwargs != null)
			kwargs.checkEmpty("future_object"); // Throws exception if there is
												// kwarg
		// defined
		if (args.len() != 1)
			throw new TypeError("future_object(): requires 1 arguments");

		FutureObject fo = Coerce.argument(args, 0, "future_object()",
				FutureObject.class);
		return new FutureQuery(fo);
	}

}
