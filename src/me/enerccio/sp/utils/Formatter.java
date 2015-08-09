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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.interpret.PythonExecutionException;
import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.parser.formatterParser;
import me.enerccio.sp.parser.formatterParser.AccessorContext;
import me.enerccio.sp.parser.formatterParser.Accessor_liteContext;
import me.enerccio.sp.parser.formatterParser.Arg_nameContext;
import me.enerccio.sp.parser.formatterParser.Arg_name_liteContext;
import me.enerccio.sp.parser.formatterParser.Element_indexContext;
import me.enerccio.sp.parser.formatterParser.Element_index_liteContext;
import me.enerccio.sp.parser.formatterParser.Field_nameContext;
import me.enerccio.sp.parser.formatterParser.Field_name_liteContext;
import me.enerccio.sp.parser.formatterParser.FintegerContext;
import me.enerccio.sp.parser.formatterParser.Format_specContext;
import me.enerccio.sp.parser.formatterParser.Format_spec_elementContext;
import me.enerccio.sp.parser.formatterParser.IntegerContext;
import me.enerccio.sp.parser.formatterParser.Replacement_fieldContext;
import me.enerccio.sp.parser.formatterParser.Replacement_field_liteContext;
import me.enerccio.sp.parser.formatterParser.SegmentContext;
import me.enerccio.sp.parser.formatterParser.SegmentsContext;
import me.enerccio.sp.parser.formatterParser.TextContext;
import me.enerccio.sp.parser.formatterParser.Text_fspecContext;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.callables.CallableObject;
import me.enerccio.sp.types.mappings.DictObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.StaticTools.ParserGenerator;

public class Formatter {

	private HashMap<String, PythonObject> dataMap;
	private List<PythonObject> indexMap;
	private formatterParser p;
	private int noClauseCounter = 0;
	private CallableObject getValue;
	private CallableObject checkUnused;
	private CallableObject formatField;
	private Set<PythonObject> used = new HashSet<PythonObject>();
	
	public Formatter(TupleObject to, DictObject kwargs, CallableObject getValue, CallableObject checkUnused, CallableObject formatField) {
		dataMap = new HashMap<String, PythonObject>(kwargs == null ? new HashMap<String, PythonObject>() : kwargs.asStringDict());
		indexMap = new ArrayList<PythonObject>(Arrays.asList(to.getObjects()));
		this.getValue = getValue;
		this.checkUnused = checkUnused;
		this.formatField = formatField;
	}
	
	public Formatter(TupleObject to, KwArgs kwargs) {
		dataMap = new HashMap<String, PythonObject>(kwargs == null ? new HashMap<String, PythonObject>() : kwargs.getAll());
		indexMap = new ArrayList<PythonObject>(Arrays.asList(to.getObjects()));
	}

	public Formatter format(String value) {
		try {
			p = ParserGenerator.parseFormatter(value);
		} catch (Exception e){
			throw Utils.throwException("RuntimeError", "format(): internal error", e);
		}
		return this;
	}

	public PythonObject consume() {
		StringObject consumed = new StringObject(doConsume());
		return consumed;
	}

	public String doConsume() {
		try {
			StringBuilder bd = new StringBuilder();
			format(bd);
			return bd.toString();
		} catch (PythonExecutionException e){
			throw e;
		} catch (Exception e){
			throw Utils.throwException("ValueError", "format(): failed parsing format string", e);
		}
	}

	private void format(StringBuilder target) {
		format(target, p.segments());
	}

	private void format(StringBuilder target, SegmentsContext segments) {
		for (SegmentContext s : segments.segment())
			format(target, s);
		if (checkUnused != null)
			PythonInterpreter.interpreter.get().execute(true, checkUnused, null, Coerce.toPython(used), Coerce.toPython(indexMap), Coerce.toPython(dataMap));
	}

	private void format(StringBuilder target, SegmentContext s) {
		if (s.text() != null){
			format(target, s.text());
		} else {
			format(target, s.replacement_field());
		}
	}

	private void format(StringBuilder target, TextContext text) {
		target.append(text.getText().replace("{{", "{").replace("}}", "}"));
	}

	private void format(StringBuilder target,
			Replacement_fieldContext ctx) {
		PythonObject dataSegment = getDataSegment(ctx.field_name());
		if (ctx.conversion() != null){
			if (ctx.conversion().conversionType().getText().equals("s")){
				dataSegment = Utils.run("str", dataSegment);
			}
		}
		if (ctx.format_spec() != null){
			format(target, dataSegment, ctx.format_spec());
		} else 
			target.append(dataSegment);
	}
	
	private void format(StringBuilder target, PythonObject dataSegment,
			Format_specContext format_spec) {
		String formatSpec = parseFormatSpec(format_spec);
		if (formatField != null){
			target.append(PythonInterpreter.interpreter.get().execute(true, formatField, null, dataSegment, 
					Coerce.toPython(formatSpec, String.class)));
		} else
			target.append(Utils.run("format", dataSegment, new StringObject(formatSpec)));
	}

	private String parseFormatSpec(Format_specContext ctx) {
		StringBuilder bd = new StringBuilder();
		for (Format_spec_elementContext se : ctx.format_spec_element())
			format(bd, se);
		return bd.toString();
	}

	private void format(StringBuilder target, Format_spec_elementContext s) {
		if (s.text_fspec() != null){
			format(target, s.text_fspec());
		} else {
			format(target, s.replacement_field_lite());
		}
	}

	private void format(StringBuilder target, Text_fspecContext text) {
		target.append(text.getText());
	}

	private void format(StringBuilder target,
			Replacement_field_liteContext ctx) {
		PythonObject dataSegment = getDataSegment(ctx.field_name_lite());
		target.append(dataSegment.toString());
	}

	private PythonObject getDataSegment(Field_name_liteContext field_name_lite) {
		if (field_name_lite == null){
			return getIndexed(noClauseCounter++);
		}
		PythonObject base = getDataSegment(field_name_lite.arg_name_lite());
		for (Accessor_liteContext ac : field_name_lite.accessor_lite()){
			if (ac.attribute_name_lite() != null){
				base = Utils.run("getattr", base, new StringObject(ac.getText().substring(1)));
			} else {
				base = PythonInterpreter.interpreter.get().execute(true, Utils.run("getattr", base, new StringObject("__getitem__")), 
						null, getElementIndex(ac.element_index_lite()));
			}
		}
		return base;
	}

	private PythonObject getElementIndex(
			Element_index_liteContext ei) {
		String text = ei.getText();
		try {
		 	return IntObject.valueOf(Integer.parseInt(text));
		} catch (NumberFormatException e){
			// pass
		}
		return new StringObject(ei.index_string_lite().getText());
	}

	private PythonObject getDataSegment(Arg_name_liteContext arg_name) {
		if (arg_name.finteger() != null){
			return getIndexed(getInteger(arg_name.finteger()));
		} else {
			return getTexted(arg_name.getText());
		}
	}

	private int getInteger(FintegerContext ic) {
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

	private PythonObject getDataSegment(Field_nameContext field_name) {
		if (field_name == null){
			return getIndexed(noClauseCounter++);
		}
		PythonObject base = getDataSegment(field_name.arg_name());
		for (AccessorContext ac : field_name.accessor()){
			if (ac.attribute_name() != null){
				base = Utils.run("getattr", base, new StringObject(ac.getText().substring(1)));
			} else {
				base = PythonInterpreter.interpreter.get().execute(true, Utils.run("getattr", base, new StringObject("__getitem__")), 
						null, getElementIndex(ac.element_index()));
			}
		}
		return base;
	}

	private PythonObject getElementIndex(Element_indexContext ei) {
		String text = ei.getText();
		try {
		 	return IntObject.valueOf(Integer.parseInt(text));
		} catch (NumberFormatException e){
			// pass
		}
		return new StringObject(ei.index_string().getText());
	}

	private PythonObject getDataSegment(Arg_nameContext arg_name) {
		if (arg_name.integer() != null){
			return getIndexed(getInteger(arg_name.integer()));
		} else {
			return getTexted(arg_name.getText());
		}
	}

	private int getInteger(IntegerContext ic) {
		String numberValue = ic.getText();
		BigInteger bi = null;
		
		if (ic.ZERO() != null)
			bi = new BigInteger("0", 10);
		if (ic.BIN_INTEGER() != null)
			bi = new BigInteger(numberValue, 2);
		if (ic.OCT_INTEGER() != null)
			bi = new BigInteger(numberValue, 8);
		if (ic.DECIMAL_INTEGER() != null)
			bi = new BigInteger(numberValue, 10);
		if (ic.HEX_INTEGER() != null)
			bi = new BigInteger(numberValue, 16);
		
		return (int)bi.longValue();
	}

	private PythonObject getIndexed(int i) {
		used.add(IntObject.valueOf(i));
		if (getValue != null){
			return PythonInterpreter.interpreter.get().execute(true, getValue, null, Coerce.toPython(i), 
					Coerce.toPython(indexMap, indexMap.getClass()), Coerce.toPython(dataMap, dataMap.getClass()));
		}
		
		if (i < indexMap.size())
			return indexMap.get(i);
		throw Utils.throwException("IndexError", "index " + i + " outside the range");
	}
	
	private PythonObject getTexted(String key) {
		used.add(new StringObject(key));
		if (getValue != null){
			return PythonInterpreter.interpreter.get().execute(true, getValue, null, Coerce.toPython(key, String.class), 
					Coerce.toPython(indexMap, indexMap.getClass()), Coerce.toPython(dataMap, dataMap.getClass()));
		}
		if (dataMap.containsKey(key))
			return dataMap.get(key);
		throw Utils.throwException("NameError", "unknown key '" + key + "'");
	}

}
