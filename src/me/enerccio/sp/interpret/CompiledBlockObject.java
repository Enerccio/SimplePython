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
package me.enerccio.sp.interpret;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.utils.Utils;

public class CompiledBlockObject extends PythonObject {
	private static final long serialVersionUID = -3047853375265834154L;
	public static final String CO_CODE = "co_code";
	public static final String CO_CONSTS = "co_consts";

	private List<PythonBytecode> bytecode;
	public CompiledBlockObject(List<PythonBytecode> bytecode){
		this.bytecode = bytecode;
	}
	
	private byte[] compiled;
	private Map<Integer, PythonObject> mmap;
	
	public byte[] getBytedata(){
		return compiled;
	}
	
	public PythonObject getConstant(int c){
		return mmap.get(c);
	}
	
	@Override
	public void newObject() {
		super.newObject();
		mmap = new HashMap<Integer, PythonObject>();
		try {
			compiled = Utils.compile(bytecode, mmap);
		} catch (Exception e) {
			throw Utils.throwException("TypeError", "invalid bytecode");
		}
		Utils.putPublic(this, CO_CODE, new StringObject(Utils.asString(compiled)));
		Utils.putPublic(this, CO_CONSTS, new MapObject(mmap));
	}

	@Override
	public PythonObject set(String key, PythonObject localContext,
			PythonObject value) {
		if (key.equals(CO_CODE) || key.equals(CO_CONSTS))
			throw Utils.throwException("AttributeError", "'" + 
					Utils.run("str", Utils.run("type", this)) + "' object attribute '" + key + "' is read only");
		return super.set(key, localContext, value);
	}
	
	@Override
	public synchronized void create(String key, me.enerccio.sp.types.AccessRestrictions restrictions, PythonObject currentContext) {
		
	}


	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	protected String doToString() {
		return "<compiled-block at 0x"+Integer.toHexString(hashCode()) + ">";
	}

}
