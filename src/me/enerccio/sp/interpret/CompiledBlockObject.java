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

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import me.enerccio.sp.compiler.Bytecode;
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
	
	public static class DebugInformation {
		public int lineno, charno;
		public String modulename;
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + charno;
			result = prime * result + lineno;
			result = prime * result
					+ ((modulename == null) ? 0 : modulename.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DebugInformation other = (DebugInformation) obj;
			if (charno != other.charno)
				return false;
			if (lineno != other.lineno)
				return false;
			if (modulename == null) {
				if (other.modulename != null)
					return false;
			} else if (!modulename.equals(other.modulename))
				return false;
			return true;
		}
		
		
	}
	
	private byte[] compiled;
	private Map<Integer, PythonObject> mmap;
	private NavigableMap<Integer, DebugInformation> dmap = new TreeMap<Integer, DebugInformation>();
	
	public byte[] getBytedata(){
		return compiled;
	}
	
	public PythonObject getConstant(int c){
		return mmap.get(c);
	}
	
	public DebugInformation getDebugInformation(int c){
		return dmap.get(dmap.floorKey(c));
	}
	
	@Override
	public void newObject() {
		super.newObject();
		mmap = new HashMap<Integer, PythonObject>();
		try {
			compiled = Utils.compile(bytecode, mmap, dmap);
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

	public static String dis(CompiledBlockObject block){
		return dis(block, false, 0);
	}
	
	public static String dis(CompiledBlockObject block, boolean single, int offset){
		StringBuilder bdd = new StringBuilder();
		
		ByteBuffer b = ByteBuffer.wrap(block.getBytedata());
		DebugInformation d = null;
		int c;
		int ord = 0;
		b.position(offset);
		
		while (b.hasRemaining()){
			StringBuilder bd = new StringBuilder();
			int pos = b.position();
			Bytecode opcode = Bytecode.fromNumber(b.get());
			
			if (d == null || !d.equals(block.getDebugInformation(pos))){
				d = block.getDebugInformation(pos);
				
				bd.append("<at " + d.modulename + ", line: " + d.lineno + ", cpos: " + d.charno + ">\n");
			}
			
			bd.append(ord++ + ".\t");
			bd.append(pos + "\t");
			bd.append(opcode + "\t");
			
			switch (opcode){
			case ACCEPT_ITER:
				bd.append(b.getInt());
				break;
			case CALL:
				bd.append("\t" + b.getInt());
				break;
			case DUP:
				bd.append("\t\t" + b.getInt());
				break;
			case ECALL:
				bd.append("\t" + b.getInt());
				break;
			case GETATTR:
				bd.append((c = b.getInt()) + " - " + block.getConstant(c));
				break;
			case GOTO:
				bd.append("\t" + b.getInt());
				break;
			case IMPORT:
				bd.append((c = b.getInt()) + " - " + block.getConstant(c) + "   ");
				bd.append((c = b.getInt()) + " - " + block.getConstant(c));
				break;
			case ISINSTANCE:
				break;
			case JUMPIFFALSE:
				bd.append(b.getInt());
				break;
			case JUMPIFNONE:
				bd.append(b.getInt());
				break;
			case JUMPIFNORETURN:
				bd.append(b.getInt());
				break;
			case JUMPIFTRUE:
				bd.append(b.getInt());
				break;
			case LOAD:
				bd.append("\t" + (c = b.getInt()) + " - " + block.getConstant(c));
				break;
			case LOADGLOBAL:
				bd.append((c = b.getInt()) + " - " + block.getConstant(c));
				break;
			case NOP:
				break;
			case POP:
				break;
			case PUSH:
				bd.append("\t" + (c = b.getInt()) + " - " + block.getConstant(c));
				break;
			case PUSH_DICT:
				bd.append((c = b.getInt()) + " - " + block.getConstant(c));
				break;
			case PUSH_ENVIRONMENT:
				break;
			case PUSH_EXCEPTION:
				break;
			case PUSH_FRAME:
				bd.append(b.getInt());
				break;
			case PUSH_LOCAL_CONTEXT:
				break;
			case RAISE:
				break;
			case RCALL:
				bd.append("\t" + b.getInt());
				break;
			case RERAISE:
				break;
			case RESOLVE_ARGS:
				break;
			case RETURN:
				bd.append(b.getInt());
				break;
			case SAVE:
				bd.append("\t" + (c = b.getInt()) + " - " + block.getConstant(c));
				break;
			case SAVEGLOBAL:
				bd.append((c = b.getInt()) + " - " + block.getConstant(c));
				break;
			case SAVE_LOCAL:
				bd.append((c = b.getInt()) + " - " + block.getConstant(c));
				break;
			case SETATTR:
				bd.append("\t" + (c = b.getInt()) + " - " + block.getConstant(c));
				break;
			case SWAP_STACK:
				break;
			case TRUTH_VALUE:
				bd.append(b.getInt());
				break;
			case UNPACK_SEQUENCE:
				bd.append(b.getInt());
				break;
			default:
				break;
			
			}
			
			bd.append("\n");
			String ss = bd.toString();
			ss = ss.substring(0, Math.min(ss.length(), 140));
			ss.trim();
			if (!ss.endsWith("\n"))
				ss += "\n";
			bdd.append(ss);
			
			if (single)
				break;
		}
		
		return bdd.toString();
	}
}
