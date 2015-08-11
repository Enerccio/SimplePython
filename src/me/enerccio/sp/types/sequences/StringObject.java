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
package me.enerccio.sp.types.sequences;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.enerccio.sp.errors.IndexError;
import me.enerccio.sp.errors.KeyError;
import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.BoolObject;
import me.enerccio.sp.types.base.NumberObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.iterators.OrderedSequenceIterator;
import me.enerccio.sp.types.mappings.DictObject;
import me.enerccio.sp.utils.ArgumentConsumer;
import me.enerccio.sp.utils.CastFailedException;
import me.enerccio.sp.utils.Coerce;
import me.enerccio.sp.utils.Formatter;
import me.enerccio.sp.utils.Utils;

/**
 * PythonObject representing java strings
 * @author Enerccio
 *
 */
public class StringObject extends ImmutableSequenceObject implements SimpleIDAccessor {
	private static final long serialVersionUID = 11L;

	private static Map<String, JavaMethodObject> sfields = new HashMap<String, JavaMethodObject>();
	
	static {
		try {
			sfields.putAll(ImmutableSequenceObject.getSFields());
			// __ADD__ is defined in SequenceObject
			sfields.put(NumberObject.__MUL__, new JavaMethodObject(StringObject.class, "mul", PythonObject.class));
			sfields.put(NumberObject.__MOD__, new JavaMethodObject(StringObject.class, "mod", PythonObject.class));
			sfields.put(NumberObject.__LT__, new JavaMethodObject(StringObject.class, "lt", PythonObject.class));
			sfields.put(NumberObject.__LE__, new JavaMethodObject(StringObject.class, "le", PythonObject.class));
			sfields.put(NumberObject.__EQ__, new JavaMethodObject(StringObject.class, "eq", PythonObject.class));
			sfields.put(NumberObject.__NE__, new JavaMethodObject(StringObject.class, "ne", PythonObject.class));
			sfields.put(NumberObject.__GE__, new JavaMethodObject(StringObject.class, "ge", PythonObject.class));
			sfields.put(NumberObject.__GT__, new JavaMethodObject(StringObject.class, "gt", PythonObject.class));
			sfields.put("capitalize", JavaMethodObject.noArgMethod(StringObject.class, "capitalize"));
			sfields.put("center", new JavaMethodObject(StringObject.class, "center"));
			sfields.put("count", new JavaMethodObject(StringObject.class, "count"));
			sfields.put("endswith", new JavaMethodObject(StringObject.class, "endswith"));
			sfields.put("expandtabs", new JavaMethodObject(StringObject.class, "expandtabs"));
			sfields.put("find", new JavaMethodObject(StringObject.class, "find"));
			sfields.put("format", new JavaMethodObject(StringObject.class, "format"));
		} catch (Exception e) {
			throw new RuntimeException("Fuck", e);
		}
	}
	protected static Map<String, JavaMethodObject> getSFields(){ return sfields; }
	@Override
	public Set<String> getGenHandleNames() {
		return sfields.keySet();
	}

	@Override
	protected Map<String, JavaMethodObject> getGenHandles() {
		return sfields;
	}
	
	public StringObject(){
		newObject();
	}
	
	public StringObject(String v){
		newObject();
		value = v;
	}
	
	@Override
	public void newObject() {
		super.newObject();
	}
	
	public String value;
	
	@Override
	public int len() {
		return value.length();
	}
	
	@Override
	public int getId(){
		return value.hashCode();
	}

	public String getString() {
		return value;
	}
	
	@Override
	public int hashCode(){
		return value.hashCode();
	}

	@Override
	public boolean equals(Object o){
		if (o instanceof StringObject)
			return value.equals(((StringObject)o).value);
		return false;
	}
	
	@Override
	protected String doToString() {
		return value;
	}

	@Override
	public PythonObject get(PythonObject key) {
		return doGet(this, key);
	}

	@Override
	public PythonObject __iter__() {
		PythonObject o = new OrderedSequenceIterator(this);
		o.newObject();
		return o;
	}
	
	@Override
	public PythonObject get(int i) {
		return valueAt(i);
	}

	@Override
	public PythonObject valueAt(int idx) {
		return new StringObject(Character.toString(value.charAt(idx)));
	}

	@Override
	public boolean containsItem(PythonObject o) {
		if (o instanceof StringObject)
			return value.contains(((StringObject)o).value);
		return false;
	}
	
	public String capitalize(){
		return value.toUpperCase();
	}
	
	public PythonObject center(TupleObject to, KwArgs kwargs){
		if (to.len() < 1 || to.len() > 2)
			throw new TypeError("center(): requires 1 or 2 arguments, " + to.len() + " provided");
		
		int llen = Coerce.argument(to, 0, "endwith", int.class);
		String fill = ArgumentConsumer.consumeArgument("center", to, kwargs, 1, "fillchar", String.class, " ");
		if (llen <= value.length())
			return new StringObject(value);
		StringBuilder strb = new StringBuilder();
		int ldiff = llen - value.length();
		for (int i=0; i<ldiff/2; i++)
			strb.append(fill);
		strb.append(value);
		for (int i=ldiff/2; i<ldiff; i++)
			strb.append(fill);
		return new StringObject(strb.toString());
	}
	
	public PythonObject count(TupleObject to, KwArgs kwargs){
		if (to.len() < 1 || to.len() > 3)
			throw new TypeError("center(): requires from 1 to 3 arguments, " + to.len() + " provided");
		
		String sub = Coerce.argument(to, 0, "endwith", String.class);
		int start = ArgumentConsumer.consumeArgument("count", to, kwargs, 1, "start", int.class, 0);
		int end = ArgumentConsumer.consumeArgument("count", to, kwargs, 2, "end", int.class, value.length()); 
		
		if (start < 0)
			start = Math.max(0, value.length()-(-(start+1)));
		if (end < 0)
			end = Math.max(0, value.length()-(-(end)));
		
		start = Math.max(start, 0);
		end = Math.min(value.length(), end);
		
		String substr = value.substring(0, end);
		int c = 0;
		for (int i=start; i<end; i++){
			if (substr.regionMatches(i, sub, 0, sub.length())){
				++c;
				i += sub.length();
			}
		}
		return NumberObject.valueOf(c);
	}
	
	public PythonObject endswith(TupleObject to, KwArgs kwargs){
		if (to.len() < 1 || to.len() > 3)
			throw new TypeError("substring(): requires from 1 to 3 arguments, " + to.len() + " provided");

		String suffix = Coerce.argument(to, 0, "endwith", String.class);
		int start = ArgumentConsumer.consumeArgument("count", to, kwargs, 1, "start", int.class, 0);
		int end = ArgumentConsumer.consumeArgument("count", to, kwargs, 2, "end", int.class, value.length()); 
		
		if (start < 0)
			start = Math.max(0, value.length()-(-(start+1)));
		if (end < 0)
			end = Math.max(0, value.length()-(-(end)));
		
		start = Math.max(start, 0);
		end = Math.min(value.length(), end);
		
		String substr = value.substring(start, end);
		return BoolObject.fromBoolean(substr.endsWith(suffix));	
	}
	
	public PythonObject expandtabs(TupleObject to, KwArgs kwargs){
		if (to.len() > 1)
			throw new TypeError("substring(): requires at most 1 argument, " + to.len() + " provided");
		int tabs = ArgumentConsumer.consumeArgument("expandtabs", to, kwargs, 0, "tabsize", int.class, 8);
		
		StringBuilder bd = new StringBuilder();
		int column = 0;
		for (int i=0; i<value.length(); i++){
			char c = value.charAt(i);
			if (c == '\n' || c == '\r'){
				bd.append(c);
				column = 0;
				continue;
			}
			
			if (c == '\t'){
				int rem = tabs - (column%tabs);
				for (int j=0; j<rem; j++)
					bd.append(" ");
				column = 0;
				continue;
			}
			
			bd.append(c);
			++column;
		}
		
		return new StringObject(bd.toString());
	}
		
	public PythonObject find(TupleObject to, KwArgs kwargs){
		if (to.len() < 1 || to.len() > 3)
			throw new TypeError("substring(): requires from 1 to 3 arguments, " + to.len() + " provided");
		String suffix = Coerce.argument(to, 0, "endwith", String.class);
		int start = ArgumentConsumer.consumeArgument("count", to, kwargs, 1, "start", int.class, 0);
		int end = ArgumentConsumer.consumeArgument("count", to, kwargs, 2, "end", int.class, value.length()); 
		
		if (start < 0)
			start = Math.max(0, value.length()-(-(start+1)));
		if (end < 0)
			end = Math.max(0, value.length()-(-(end)));
		
		start = Math.max(start, 0);
		end = Math.min(value.length(), end);
		
		String substr = value.substring(start, end);
		return NumberObject.valueOf(substr.indexOf(suffix));
	}
	
	public PythonObject format(TupleObject to, KwArgs kwargs){
		return new Formatter(to, kwargs).format(value).consume();
	}
	
	@Override
	public void deleteKey(PythonObject key) {
		throw new TypeError("'" + Utils.run("typename", this) + "' object doesn't support item deletion");
	}
	
	public PythonObject mul(PythonObject b){
		if (b instanceof NumberObject) {
			if (((NumberObject)b).getNumberType() == NumberObject.NumberType.INT) {
				// "a" * 5 -> "aaaaa"
				StringBuilder sb = new StringBuilder();
				for (int i=0; i<((NumberObject)b).intValue(); i++)
					sb.append(value);
				return new StringObject(sb.toString());
			}
		}
		throw new TypeError("can't multiply sequence by non-int of type '" + b + "'");
	}
	
	public PythonObject mod(PythonObject b){
		String formatText = value;
		StringBuilder result = new StringBuilder();
		
		DictObject dictSource = null;
		TupleObject tupleSource = null;

		if (b instanceof DictObject) {
			dictSource = (DictObject)b;
			tupleSource = new TupleObject(b);
		} else {
			if (b instanceof TupleObject)
				tupleSource = (TupleObject)b;
			else {
				tupleSource = new TupleObject(b);
			}
		}
		
		try {
			simpleFormat(formatText, result, tupleSource, dictSource);
		} catch (ArrayIndexOutOfBoundsException e){
			throw new TypeError("__mod__(): failed to parse format string");
		} catch (CastFailedException e) {
			throw new TypeError("__mod__(): failed to convert object to correct type");
		}
		return new StringObject(result.toString());
	}
	
	private enum FormatStep {
		TEXT, SPEC, MODS
	}
	
	private static Set<Character> formatChars = new HashSet<Character>(Arrays.asList(new Character[]{
			'd', 'i', 'o', 'u', 'x', 'X', 'e', 'E', 'f', 'F', 'g', 'G', 'c', 'r', 's', '%'
	}));
//	private static Set<Character> integral = new HashSet<Character>(Arrays.asList(new Character[]{
//			'd', 'o', 'x', 'X'
//	}));
	private static Set<Character> floatingPoint = new HashSet<Character>(Arrays.asList(new Character[]{
			'e', 'E', 'f', 'F', 'g', 'G'
	}));
	
	private void simpleFormat(String formatText, StringBuilder result,
			TupleObject tupleSource, DictObject dictSource) throws CastFailedException {
		FormatStep cstep = FormatStep.TEXT;
		char c;
		int tupleCount = 0;
		PythonObject consumed = null;
		char mode;
		
		for (int i=0; i<formatText.length(); i++){
			c = formatText.charAt(i);
			
			if (cstep == FormatStep.TEXT && c == '%'){
				cstep = FormatStep.SPEC;
			} else if (cstep == FormatStep.SPEC && c == '('){
				if (dictSource == null)
					throw new TypeError("__mod__(): format text requiring dict, but dict was not passed");
				tupleSource = null;
				String keyname = "";
				while (true){
					c = formatText.charAt(i++);
					if (c == ')')
						break;
					keyname += c;
				}
				consumed = dictSource.getItem(keyname);
				if (consumed == null)
					throw new KeyError("__mod__(): format key '" + keyname + "' not found");
				cstep = FormatStep.MODS;
			} else if (cstep == FormatStep.SPEC){
				cstep = FormatStep.MODS;
				if (tupleSource == null)
					throw new TypeError("__mod__(): can't mix tuple and dict sources");
				--i;
			} else if (cstep == FormatStep.MODS){
				String rest = "";
				while (true){
					c = formatText.charAt(i++);
					
					if (formatChars.contains(c)){
						mode = c;
						break;
					} else {
						rest += c;
					}
				}
				
				if (mode == '%'){
					result.append("%");
					continue;
				}
				
				while (rest.contains("*")){
					if (tupleCount >= tupleSource.len())
						throw new IndexError("__mod__(): tuple index out of range");
					PythonObject replacedBy = tupleSource.get(tupleCount++);
					rest = rest.replaceFirst("\\*", replacedBy.toString());
				}
				
				if (consumed == null){
					if (tupleCount >= tupleSource.len())
						throw new IndexError("__mod__(): tuple index out of range");
					consumed = tupleSource.get(tupleCount++);
				}
				
				if (mode == 'i')
					mode = 'd';
				if (mode == 'u')
					mode = 'd';
				
				Object arg = null;
				if (mode == 'r' || mode == 's'){
					consumed = Utils.run("str", consumed);
					arg = Coerce.toJava(consumed, String.class); 
				} else if (mode == 'c'){
					if (consumed instanceof StringObject){
						StringObject s = (StringObject)consumed;
						if (s.len() != 1)
							throw new IndexError("__mod__(): string argument for 'c' must be 1 character long"); 
						arg = s.value;
					} else if (consumed instanceof NumberObject) {
						arg = ((NumberObject)consumed).intValue();
					}
				} else {
					NumberObject no = Coerce.toJava(consumed, NumberObject.class);
					if (floatingPoint.contains(mode))
						arg = no.doubleValue();
					else
						arg = no.longValue();
				}
				
				try {
					result.append(String.format("%" + rest + mode, arg));
				} catch (Exception e){
					throw new TypeError("__mod__(): wrong format syntax caused by " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
				}
				cstep = FormatStep.TEXT;
			} else if (cstep == FormatStep.TEXT) {
				result.append(c);
			} else {
				throw new TypeError("__mod__(): wrong format syntax near '" + c + "'");
			}
			
		}
	}
	
	public PythonObject add(PythonObject b) {
		if (b instanceof NumberObject)
			return new StringObject(value + b.toString());
		if (b instanceof StringObject)
			return new StringObject(value + b.toString());
		throw new TypeError("cannot concatenate 'str' and " + b);
	}

	public PythonObject lt(PythonObject b) {
		if (b instanceof StringObject)
			return value.compareTo(((StringObject)b).value) < 0 ? BoolObject.TRUE : BoolObject.FALSE;
		return BoolObject.FALSE;

	}

	public PythonObject le(PythonObject b) {
		if (b instanceof StringObject)
			return value.compareTo(((StringObject)b).value) <= 0 ? BoolObject.TRUE : BoolObject.FALSE;
		return BoolObject.FALSE;
	}

	public PythonObject eq(PythonObject b) {
		if (b instanceof StringObject)
			return ((StringObject)b).value.equals(value) ? BoolObject.TRUE : BoolObject.FALSE;
		return BoolObject.FALSE;
	}

	@Override
	public PythonObject ne(PythonObject b) {
		if (b instanceof StringObject)
			return ((StringObject)b).value.equals(value) ? BoolObject.FALSE : BoolObject.TRUE;
		return BoolObject.FALSE;
	}

	public PythonObject gt(PythonObject b) {
		if (b instanceof StringObject)
			return value.compareTo(((StringObject)b).value) > 0 ? BoolObject.TRUE : BoolObject.FALSE;
		return BoolObject.FALSE;
	}

	public PythonObject ge(PythonObject b) {
		if (b instanceof StringObject)
			return value.compareTo(((StringObject)b).value) >= 0 ? BoolObject.TRUE : BoolObject.FALSE;
		return BoolObject.FALSE;
	}
}
