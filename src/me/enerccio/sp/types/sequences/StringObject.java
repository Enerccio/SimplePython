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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.BoolObject;
import me.enerccio.sp.types.base.NumberObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.iterators.OrderedSequenceIterator;
import me.enerccio.sp.utils.ArgumentConsumer;
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
			throw Utils.throwException("TypeError", "center(): requires 1 or 2 arguments, " + to.len() + " provided");
		
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
			throw Utils.throwException("TypeError", "center(): requires from 1 to 3 arguments, " + to.len() + " provided");
		
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
			throw Utils.throwException("TypeError", "substring(): requires from 1 to 3 arguments, " + to.len() + " provided");

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
			throw Utils.throwException("TypeError", "substring(): requires at most 1 argument, " + to.len() + " provided");
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
			throw Utils.throwException("TypeError", "substring(): requires from 1 to 3 arguments, " + to.len() + " provided");
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
		throw Utils.throwException("TypeError", "'" + Utils.run("typename", this) + "' object doesn't support item deletion");
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
		throw new TypeError("string format not yet supported"); // :(
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
