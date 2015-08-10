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
package me.enerccio.sp.utils;

import java.math.BigInteger;

import me.enerccio.sp.interpret.PythonExecutionException;
import me.enerccio.sp.parser.formatParser;
import me.enerccio.sp.parser.formatParser.FintegerContext;
import me.enerccio.sp.parser.formatParser.Format_specificationContext;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.base.NumberObject;
import me.enerccio.sp.types.base.NumberObject.NumberType;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.utils.StaticTools.ParserGenerator;

public class Format {
	
	private PythonObject value;
	public Format(PythonObject value){
		this.value = value;
	}

	private formatParser p;
	public Format format(String value) {
		try {
			p = ParserGenerator.parseFormat(value);
		} catch (Exception e){
			throw Utils.throwException("RuntimeError", "__format__(): internal error", e);
		}
		return this;
	}
	
	public PythonObject consume() {
		StringObject consumed = new StringObject(doConsume());
		return consumed;
	}

	private String doConsume() {
		try {
			StringBuilder bd = new StringBuilder();
			format(bd);
			return bd.toString();
		} catch (PythonExecutionException e){
			throw e;
		} catch (Exception e){
			throw Utils.throwException("ValueError", "__format__(): failed parsing format string", e);
		}
	}

	private void format(StringBuilder bd) {
		format(bd, p.format_specification());
	}

	private void format(StringBuilder bd,
			Format_specificationContext ctx) {
		format(bd, value, ctx);
	}

	private enum SignMode { PLUS, MINUS, SPACE };
	private enum Align { LEFT, RIGHT, EQ, CENTER};

	private void format(StringBuilder target, PythonObject dataSegment,
			Format_specificationContext ctx) {
		String text;
		SignMode sign = SignMode.MINUS;
		boolean signSpecified = false;
		boolean prefixed = false;
		Integer width = null;
		Integer precision = null;
		String mode = "s";
		String fillc = " ";
		Align align = Align.LEFT;
		boolean alignSpecified = false;
		
		if (dataSegment instanceof NumberObject){
			align = Align.RIGHT;
			if (((NumberObject)dataSegment).getNumberType() == NumberType.FLOAT)
				mode = "g";
			else if (((NumberObject)dataSegment).getNumberType() == NumberType.INT)
				mode = "d";
		}
		
		if (ctx.sign() != null){
			if (ctx.sign().getText().equals("-"))
				sign = SignMode.MINUS;
			if (ctx.sign().getText().equals("+"))
				sign = SignMode.PLUS;
			if (ctx.sign().getText().equals(" "))
				sign = SignMode.SPACE;
			signSpecified = true;
		}
		
		if (ctx.hash() != null)
			prefixed = true;
		if (ctx.width() != null)
			width = getIntValue(ctx.width().finteger());
		if (ctx.precision() != null)
			precision = getIntValue(ctx.precision().finteger());
		
		if (ctx.type() != null)
			mode = ctx.type().getText();
		
		switch (mode){
		case "s":
			text = dataSegment.toString();
			break;
		case "b":
			if (!NumberObject.isInteger(dataSegment))
				unsupportedMode(mode, dataSegment);
			text = String.format("b", ((NumberObject)dataSegment).intValue());
			break;
		}
		
		if (width != null && ctx.width().getText().startsWith("0")){
			fillc = "0";
			align = Align.EQ;
		}
		
		if (!(dataSegment instanceof NumberObject)){
			if (align == Align.EQ)
				throw Utils.throwException("TypeError", "__format__(): align = only available for numeric types, not type '" + Utils.run("typename", dataSegment) + "'");
			if (signSpecified)
				throw Utils.throwException("TypeError", "__format__(): sign only available for numeric types, not type '" + Utils.run("typename", dataSegment) + "'");
		}
	}

	private void unsupportedMode(String mode, PythonObject dataSegment) {
		throw Utils.throwException("TypeError", "__format__(): unsupported mode '" + mode + "' for type '" + Utils.run("typename", dataSegment) + "'");
	}

	private Integer getIntValue(FintegerContext ic) {
		String numberValue = ic.getText();
		BigInteger bi = null;
		
		if (ic.FZERO() != null)
			bi = new BigInteger("0", 10);
		if (ic.FBIN_INTEGER() != null)
			bi = new BigInteger(numberValue, 2);
		if (ic.FOCT_INTEGER() != null)
			bi = new BigInteger(numberValue, 8);
		if (ic.FDECIMAL_INTEGER() != null)
			bi = new BigInteger(numberValue, 10);
		if (ic.FHEX_INTEGER() != null)
			bi = new BigInteger(numberValue, 16);
		
		return (int)bi.longValue();
	}
}
