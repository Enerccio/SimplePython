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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.interpret.PythonExecutionException;
import me.enerccio.sp.parser.formatterLexer;
import me.enerccio.sp.parser.formatterParser;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils.ThrowingErrorListener;

public class Formatter {

	private HashMap<String, PythonObject> dataMap;
	private List<PythonObject> indexMap;
	private formatterParser p;
	
	public Formatter(TupleObject to, KwArgs kwargs) {
		dataMap = new HashMap<String, PythonObject>(kwargs == null ? new HashMap<String, PythonObject>() : kwargs.getAll());
		indexMap = new ArrayList<PythonObject>(Arrays.asList(to.getObjects()));
	}

	public Formatter format(String value) {
		try {
			ANTLRInputStream is = new ANTLRInputStream(new ByteArrayInputStream(value.getBytes()));
			formatterLexer lexer = new formatterLexer(is);
			lexer.removeErrorListeners();
			lexer.addErrorListener(new ThrowingErrorListener("<input string>"));
			CommonTokenStream stream = new CommonTokenStream(lexer);
			formatterParser parser = new formatterParser(stream);
			
			parser.removeErrorListeners();
			parser.addErrorListener(new ThrowingErrorListener("<input string>"));
			p = parser;
		} catch (Exception e){
			throw Utils.throwException("RuntimeError", "format(): internal error", e);
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
			format(new StringBuilder());
			return bd.toString();
		} catch (PythonExecutionException e){
			throw e;
		} catch (Exception e){
			throw Utils.throwException("RuntimeError", "format(): internal error", e);
		}
	}

	private void format(StringBuilder target) {
		
	}

}
