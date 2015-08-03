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
import me.enerccio.sp.types.mappings.DictObject;
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
	
	public synchronized PythonObject getConstant(int c){
		return mmap.get(c);
	}
	
	public synchronized DebugInformation getDebugInformation(int c){
		return dmap.get(dmap.floorKey(c));
	}
	
	@Override
	public void newObject() {
		super.newObject();
		mmap = new HashMap<Integer, PythonObject>();
		try {
			compiled = Utils.compile(bytecode, mmap, dmap);
		} catch (Exception e) {
			throw Utils.throwException("TypeError", "invalid bytecode", e);
		}
		Utils.putPublic(this, CO_CODE, new StringObject(Utils.asString(compiled)));
		Utils.putPublic(this, CO_CONSTS, new DictObject(mmap));
	}

	@Override
	public synchronized PythonObject set(String key, PythonObject localContext,
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

	public static synchronized String dis(CompiledBlockObject block){
		return dis(block, false, 0);
	}
	
	public static synchronized String dis(CompiledBlockObject block, boolean single, int offset) {
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
			
			bd.append(String.format("{fc: %s, ac: %s} ", PythonInterpreter.interpreter.get().currentFrame.size(), PythonInterpreter.interpreter.get().getAccessCount()));
			
			if (d == null || !d.equals(block.getDebugInformation(pos))){
				d = block.getDebugInformation(pos);
				
				bd.append(
					String.format("<at %s %-7.7s> ",
						d.modulename, " " + d.lineno + ":" + d.charno));
				
			}
			
			if (!single)
				bd.append(String.format("%-5.5s", (ord++)));
			bd.append(String.format("%-5.5s", (pos)));
			bd.append(String.format("%-9.9s ", (opcode)));
			
			final String FORMAT = "%-25.25s";
			switch (opcode){
			case CALL:
			case PUSH_LOCALS:
			case OPEN_LOCALS:
			case DUP:
			case ECALL:
			case GOTO:
			case JUMPIFFALSE:
			case JUMPIFNONE:
			case JUMPIFNORETURN:
			case JUMPIFTRUE:
			case PUSH_DICT:
			case PUSH_FRAME:
			case RCALL:
			case UNPACK_SEQUENCE:
				bd.append(String.format(FORMAT, b.getInt()));
				break;
			case ACCEPT_ITER:
			case GET_ITER:
				bd.append(String.format(FORMAT, "or jump to " + b.getInt()));
				break;
			case SETUP_LOOP:
				bd.append(String.format(FORMAT, "or jump to " + b.getInt() + " with javaiterator"));
				break;
			case TRUTH_VALUE:
				c = b.getInt();
				if (c == 1)
					bd.append(String.format(FORMAT, "1 - negated"));
				else
					bd.append(String.format(FORMAT, c));
				break;
			case RETURN:
				c = b.getInt();
				if (c == 1)
					bd.append(String.format(FORMAT, "1 - returns value"));
				else
					bd.append(String.format(FORMAT, "" + c + " - exits frame"));
				break;
			case GETATTR:
			case KWARG:
			case LOAD:
			case LOADGLOBAL:
			case LOADDYNAMIC:
			case SAVE:
			case PUSH:
			case SAVEGLOBAL:
			case SAVE_LOCAL:
			case SAVEDYNAMIC:
			case SETATTR:
			case YIELD:
				c = b.getInt();
				bd.append(String.format(FORMAT, String.format("%s (id %s)" , block.getConstant(c), c)));
				break;
			case IMPORT:
				bd.append(String.format(FORMAT, (c = b.getInt()) + " - " + block.getConstant(c) + "   " + (c = b.getInt()) + " - " + block.getConstant(c)));
				break;
			case ISINSTANCE:
			case NOP:
			case POP:
			case PUSH_ENVIRONMENT:
			case PUSH_EXCEPTION:
			case PUSH_LOCAL_CONTEXT:
			case RAISE:
			case RERAISE:
			case RESOLVE_ARGS:
			case SWAP_STACK:
				bd.append(String.format(FORMAT, ""));
				break;
			//default:
			//	bd.append(String.format(FORMAT, " --- FIXME: unknown opcode --- "));
			//	break;
			
			}
			
			if (single)
				return bd.toString();
			
			bd.append("\n");
			String ss = bd.toString();
			ss = ss.substring(0, Math.min(ss.length(), 140));
			ss.trim();
			if (!ss.endsWith("\n"))
				ss += "\n";
			bdd.append(ss);
		}
		
		return bdd.toString();
	}
}
