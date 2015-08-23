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
package me.enerccio.sp.types.base;

import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.utils.Utils;

/**
 * Complex Number type
 * 
 * @author Enerccio
 *
 */
public class ComplexObject extends NumberObject {
	private static final long serialVersionUID = 9L;
	private static final String REAL_ACCESSOR = "real";
	private static final String IMAG_ACCESSOR = "imag";

	@Override
	public NumberType getNumberType() {
		return NumberType.COMPLEX;
	}

	public ComplexObject() {

	}

	public ComplexObject(double r, double i) {
		this(NumberObject.valueOf(r), NumberObject.valueOf(i));
	}

	public ComplexObject(NumberObject r, NumberObject i) {
		fields.put(REAL_ACCESSOR, new AugumentedPythonObject(r,
				AccessRestrictions.PUBLIC));
		fields.put(IMAG_ACCESSOR, new AugumentedPythonObject(i,
				AccessRestrictions.PUBLIC));
	}

	@Override
	protected void registerObject() {

	}

	@Override
	public boolean truthValue() {
		return Utils.get(this, REAL_ACCESSOR).truthValue()
				|| Utils.get(this, IMAG_ACCESSOR).truthValue();
	}

	@Override
	public int hashCode() {
		return fields.get(REAL_ACCESSOR).object.hashCode()
				^ fields.get(IMAG_ACCESSOR).object.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ComplexObject) {
			return ((ComplexObject) o).fields.get(REAL_ACCESSOR).object
					.equals(fields.get(REAL_ACCESSOR).object)
					&& ((ComplexObject) o).fields.get(IMAG_ACCESSOR).object
							.equals(fields.get(IMAG_ACCESSOR).object);
		}
		return false;
	}

	@Override
	protected String doToString() {
		return "(" + fields.get(REAL_ACCESSOR).object.toString() + "+"
				+ fields.get(IMAG_ACCESSOR).object.toString() + "j)";
	}

	@Override
	public int intValue() {
		throw new TypeError("can't convert complex to int");
	}

	@Override
	public long longValue() {
		throw new TypeError("can't convert complex to long");
	}

	@Override
	public float floatValue() {
		throw new TypeError("can't convert complex to float");
	}

	@Override
	public double doubleValue() {
		throw new TypeError("can't convert complex to float");
	}

	@Override
	public double getRealValue() {
		return ((NumberObject) fields.get(REAL_ACCESSOR).object).doubleValue();
	}

	@Override
	public double getImaginaryValue() {
		return ((NumberObject) fields.get(IMAG_ACCESSOR).object).doubleValue();
	}

	@Override
	public PythonObject add(PythonObject b) {
		if (b instanceof NumberObject) {
			NumberObject n = (NumberObject) b;
			return new ComplexObject(getRealValue() + n.getRealValue(),
					getImaginaryValue() + n.getImaginaryValue());
		}
		return invalidOperation("+", b);
	}

	@Override
	public PythonObject sub(PythonObject b) {
		if (b instanceof NumberObject) {
			NumberObject n = (NumberObject) b;
			return new ComplexObject(getRealValue() - n.getRealValue(),
					getImaginaryValue() - n.getImaginaryValue());
		}
		return invalidOperation("-", b);
	}

	@Override
	public PythonObject mul(PythonObject b) {
		if (b instanceof NumberObject) {
			NumberObject n = (NumberObject) b;
			return new ComplexObject(getRealValue() * n.getRealValue(),
					getImaginaryValue() * n.getImaginaryValue());
		}
		return invalidOperation("*", b);
	}

	@Override
	public PythonObject div(PythonObject b) {
		if (b instanceof NumberObject) {
			NumberObject n = (NumberObject) b;
			return new ComplexObject(getRealValue() / n.getRealValue(),
					getImaginaryValue() / n.getImaginaryValue());

		}
		return invalidOperation("/", b);
	}

	@Override
	public PythonObject mod(PythonObject b) {
		if (b instanceof NumberObject) {
			NumberObject n = (NumberObject) b;
			return new ComplexObject(getRealValue() % n.getRealValue(),
					getImaginaryValue() % n.getImaginaryValue());
		}
		return invalidOperation("%", b);
	}

	@Override
	public PythonObject lt(PythonObject b) {
		throw new TypeError(
				"no ordering relation is defined for complex numbers");
	}

	@Override
	public PythonObject le(PythonObject b) {
		throw new TypeError(
				"no ordering relation is defined for complex numbers");
	}

	@Override
	public PythonObject eq(PythonObject b) {
		throw new TypeError(
				"no ordering relation is defined for complex numbers");
	}

	@Override
	public PythonObject ne(PythonObject b) {
		throw new TypeError(
				"no ordering relation is defined for complex numbers");
	}

	@Override
	public PythonObject gt(PythonObject b) {
		throw new TypeError(
				"no ordering relation is defined for complex numbers");
	}

	@Override
	public PythonObject ge(PythonObject b) {
		throw new TypeError(
				"no ordering relation is defined for complex numbers");
	}
}
