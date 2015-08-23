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
import me.enerccio.sp.types.base.ComplexObject;
import me.enerccio.sp.types.base.NumberObject;
import me.enerccio.sp.types.sequences.TupleObject;

/**
 * complex()
 * 
 * @author Enerccio
 *
 */
public class ComplexTypeObject extends TypeObject {
	private static final long serialVersionUID = -6989323431265671329L;
	public static final String COMPLEX_CALL = "complex";

	@Override
	public String getTypeIdentificator() {
		return "complex";
	}

	@Override
	public PythonObject call(TupleObject o, KwArgs kwargs) {
		if (kwargs != null)
			kwargs.notExpectingKWArgs(); // Throws exception if there is kwarg
											// defined
		if (o.len() > 2)
			throw new TypeError("complex(): requires up to 2 parameters");

		double real = 0;
		double imag = 0;

		try {
			if (o.len() == 2) {
				real = ((NumberObject) o.valueAt(0)).doubleValue();
				imag = ((NumberObject) o.valueAt(0)).doubleValue();
			} else if (o.len() == 1) {
				real = ((NumberObject) o.valueAt(0)).doubleValue();
			}
		} catch (ClassCastException e) {
			throw new TypeError("complex(): parameters must be numbers");
		}

		return new ComplexObject(real, imag);
	}

}
