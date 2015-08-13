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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import me.enerccio.sp.compiler.Bytecode;
import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.errors.AttributeError;
import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.runtime.ModuleInfo;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.mappings.DictObject;
import me.enerccio.sp.types.pointer.PointerObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.utils.Utils;

public class CompiledBlockObject extends PythonObject {
	private static final long serialVersionUID = -3047853375265834154L;
	public static final String CO_CODE = "co_code";
	public static final String CO_CONSTS = "co_consts";
	public static final String CO_DEBUG = "co_debug";

	public CompiledBlockObject(List<PythonBytecode> bytecode){
		super(false);
		mmap = new HashMap<Integer, PythonObject>();
		try {
			if (compiled == null) {
				compiled = compile(bytecode, mmap, dmap);
				bytecode = null;
			}
		} catch (Exception e) {
			throw new TypeError("invalid bytecode", e);
		}
		Utils.putPublic(this, CO_CODE, new StringObject(Utils.asString(compiled)));
		Utils.putPublic(this, CO_CONSTS, new DictObject(mmap));
		Utils.putPublic(this, CO_DEBUG, new PointerObject(dmap));
	}
	
	public CompiledBlockObject(byte[] compiled, Map<Integer, PythonObject> mmap) {
		super(false);
		this.compiled = compiled;
		this.mmap = mmap;
	}

	public static class DebugInformation {
		public int lineno, charno;
		public ModuleInfo module;
		public String function;
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + charno;
			result = prime * result + lineno;
			result = prime * result + function.hashCode();
			result = prime * result + module.hashCode();
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
			if (!function.equals(other.function))
				return false;
			if (!module.equals(other.module))
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
	public synchronized PythonObject set(String key, PythonObject localContext,
			PythonObject value) {
		if (key.equals(CO_CODE) || key.equals(CO_CONSTS) || key.equals(CO_DEBUG))
			throw new AttributeError("'" + Utils.run("str", Utils.run("typename", this)) + "' object attribute '" + key + "' is read only");
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
			Bytecode opcode = Bytecode.fromNumber(((short) (b.get() & 0xff)));
			
			bd.append(String.format("{fc: %s, ac: %s} ", PythonInterpreter.interpreter.get().currentFrame.size(), PythonInterpreter.interpreter.get().getAccessCount()));
			
			if (d == null || !d.equals(block.getDebugInformation(pos))){
				d = block.getDebugInformation(pos);
				
				bd.append(
					String.format("<at %s %-7.7s> ",
						d.module.getName(), " " + d.lineno + ":" + d.charno));
				
			}
			
			if (!single)
				bd.append(String.format("%-5.5s", (ord++)));
			bd.append(String.format("%-5.5s", (pos)));
			bd.append(String.format("%-9.9s ", (opcode)));
			
			final String FORMAT = "%-25.25s";
			switch (opcode){
			case CALL:
			case DUP:
			case ECALL:
			case GOTO:
			case JUMPIFFALSE:
			case JUMPIFNONE:
			case JUMPIFNORETURN:
			case JUMPIFTRUE:
			case KCALL:
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
			case DEL:
				c = b.getInt();
				boolean isGlobal = b.getInt() == 1;
				bd.append(String.format(FORMAT, String.format("%s (id %s)" , block.getConstant(c), c)));
				if (isGlobal){
					bd.append(String.format(" - global"));
				}
				break;
			case DELATTR:
			case GETATTR:
			case KWARG:
			case LOAD:
			case LOADGLOBAL:
			case LOADBUILTIN:
			case LOADDYNAMIC:
			case MAKE_FUTURE:
			case PUSH:
			case SAVE:
			case SAVEGLOBAL:
			case SAVE_LOCAL:
			case SAVEDYNAMIC:
			case SETATTR:
			case TEST_FUTURE:
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
			case PUSH_LOCALS:
			case OPEN_LOCALS:
			case RESOLVE_CLOSURE:
			case UNPACK_KWARG:
			case ADD:
			case AND:
			case DIV:
			case EQ:
			case GE:
			case GREATER:
			case LESS:
			case LSHIFT:
			case LT:
			case MOD:
			case MUL:
			case NEW:
			case OR:
			case POW:
			case RSHIFT:
			case SUB:
			case XOR:
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
	

	private static ThreadLocal<Integer> kkey = new ThreadLocal<Integer>();
	public static byte[] compile(List<PythonBytecode> bytecode,
			Map<Integer, PythonObject> mmap, NavigableMap<Integer, DebugInformation> dmap) throws Exception {
		Map<PythonObject, Integer> rmap = new HashMap<PythonObject, Integer>();
		Map<Integer, Integer> rmapMap = new TreeMap<Integer, Integer>();
		Map<Integer, Integer> jumpMap = new TreeMap<Integer, Integer>();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream w = new DataOutputStream(baos);
		
		DebugInformation d = null;
		kkey.set(0);
		PythonBytecode last = bytecode.get(bytecode.size() - 1);
		if (last.getOpcode() != Bytecode.NOP)
			bytecode.add(Bytecode.makeBytecode(Bytecode.NOP, null, null, last.debugModule));
		
		int itc = 0;
		for (PythonBytecode b : bytecode){
			int ii = baos.size();
			rmapMap.put(itc, ii);
			
			if (d == null || notEqual(d, b)){
				d = new DebugInformation();
				d.charno = b.debugCharacter;
				d.lineno = b.debugLine;
				d.function = b.debugFunction;
				d.module = b.debugModule;
				dmap.put(ii, d);
			}
			
			w.writeByte(b.getOpcode().id);
			
			switch(b.getOpcode()){
			case ACCEPT_ITER:
				jumpMap.put(itc, b.intValue);
				w.writeInt(b.intValue);
				break;
			case CALL:
				w.writeInt(b.intValue);
				break;
			case DUP:
				w.writeInt(b.intValue);
				break;
			case YIELD:
				w.writeInt(insertValue(new StringObject(b.stringValue), mmap, rmap));
				w.writeInt(b.intValue);
				break;
			case ECALL:
				w.writeInt(b.intValue);
				break;
			case GET_ITER:
				jumpMap.put(itc, b.intValue);
				w.writeInt(0);
				break;
			case GETATTR:
				w.writeInt(insertValue(b.stringValue == null ? NoneObject.NONE : new StringObject(b.stringValue), mmap, rmap));
				break;
			case GOTO:
				jumpMap.put(itc, b.intValue);
				w.writeInt(0);
				break;
			case IMPORT:
				w.writeInt(insertValue(new StringObject(b.stringValue), mmap, rmap));
				w.writeInt(insertValue(new StringObject((String)b.object), mmap, rmap));
				break;
			case ISINSTANCE:
				break;
			case JUMPIFFALSE:
				jumpMap.put(itc, b.intValue);
				w.writeInt(0);
				break;
			case JUMPIFNONE:
				jumpMap.put(itc, b.intValue);
				w.writeInt(0);
				break;
			case JUMPIFNORETURN:
				jumpMap.put(itc, b.intValue);
				w.writeInt(0);
				break;
			case JUMPIFTRUE:
				jumpMap.put(itc, b.intValue);
				w.writeInt(0);
				break;
			case KCALL:
				w.writeInt(b.intValue);
				break;
			case LOAD:
				w.writeInt(insertValue(new StringObject(b.stringValue), mmap, rmap));
				break;
			case LOADGLOBAL:
				w.writeInt(insertValue(new StringObject(b.stringValue), mmap, rmap));
				break;
			case LOADDYNAMIC:
				w.writeInt(insertValue(new StringObject(b.stringValue), mmap, rmap));
				break;
			case LOADBUILTIN:
				w.writeInt(insertValue(new StringObject(b.stringValue), mmap, rmap));
				break;
			case TEST_FUTURE:
				w.writeInt(insertValue(new StringObject(b.stringValue), mmap, rmap));
				break;
			case NOP:
				break;
			case POP:
				break;
			case OPEN_LOCALS:
				break;
			case PUSH_LOCALS:
				break;
			case PUSH:
				w.writeInt(insertValue(b.value, mmap, rmap));
				break;
			case MAKE_FUTURE:
			case KWARG:
				String[] ss = (String[]) b.object;
				w.writeInt(ss.length);
				for (String s : ss)
					w.writeInt(insertValue(new StringObject(s), mmap, rmap));
				break;
			case RESOLVE_CLOSURE:
				break;
			case PUSH_ENVIRONMENT:
				break;
			case PUSH_EXCEPTION:
				break;
			case PUSH_FRAME:
				jumpMap.put(itc, b.intValue);
				w.writeInt(b.intValue);
				break;
			case PUSH_LOCAL_CONTEXT:
				break;
			case RAISE:
				break;
			case RCALL:
				w.writeInt(b.intValue);
				break;
			case RERAISE:
				break;
			case RESOLVE_ARGS:
				break;
			case RETURN:
				w.writeInt(b.intValue);
				break;
			case DEL:
				w.writeInt(insertValue(new StringObject(b.stringValue), mmap, rmap));
				w.writeInt(b.booleanValue ? 1 : 0);
				break;
			case DELATTR:
				w.writeInt(insertValue(new StringObject(b.stringValue), mmap, rmap));
				break;
			case SAVE:
				w.writeInt(insertValue(new StringObject(b.stringValue), mmap, rmap));
				break;
			case SAVEGLOBAL:
				w.writeInt(insertValue(new StringObject(b.stringValue), mmap, rmap));
				break;
			case SAVEDYNAMIC:
				w.writeInt(insertValue(new StringObject(b.stringValue), mmap, rmap));
				break;
			case SAVE_LOCAL:
				w.writeInt(insertValue(new StringObject(b.stringValue), mmap, rmap));
				break;
			case SETATTR:
				w.writeInt(insertValue(b.stringValue == null ? NoneObject.NONE : new StringObject(b.stringValue), mmap, rmap));
				break;
			case SETUP_LOOP:
				jumpMap.put(itc, b.intValue);
				w.writeInt(0);
				break;
			case SWAP_STACK:
				break;
			case TRUTH_VALUE:
				w.writeInt(b.intValue);
				break;
			case UNPACK_KWARG:
				break;
			case UNPACK_SEQUENCE:
				w.writeInt(b.intValue);
				break;
			case ADD:
				break;
			case AND:
				break;
			case DIV:
				break;
			case EQ:
				break;
			case GE:
				break;
			case GREATER:
				break;
			case LESS:
				break;
			case LSHIFT:
				break;
			case LT:
				break;
			case MOD:
				break;
			case MUL:
				break;
			case NEW:
				break;
			case OR:
				break;
			case POW:
				break;
			case RSHIFT:
				break;
			case SUB:
				break;
			case XOR:
				break;
			}
			
			++itc;
		}
		
		byte[] data = baos.toByteArray();
		ByteBuffer b = ByteBuffer.wrap(data);
		
		for (Integer ppos : jumpMap.keySet()){
			Integer jumpval = jumpMap.get(ppos);
			Integer location = rmapMap.get(jumpval);
			Integer wloc = rmapMap.get(ppos) + 1;
			b.position(wloc);
			b.putInt(location);
		}
		
		return data;
	}

	private static boolean notEqual(DebugInformation d, PythonBytecode b) {
		return d.charno != b.debugCharacter || d.lineno != b.debugLine
				 || !d.module.equals(b.debugModule)  || !d.function.equals(b.debugFunction) ;
	}

	private static int insertValue(PythonObject v, Map<Integer, PythonObject> mmap, Map<PythonObject, Integer> rmap) {
		if (rmap.containsKey(v))
			return rmap.get(v);
		int key = kkey.get();
		kkey.set(key+1);
		rmap.put(v, key);
		mmap.put(key, v);
		return key;
	}
	
	@Override
	public Set<String> getGenHandleNames() {
		return PythonObject.sfields.keySet();
	}

	@Override
	protected Map<String, JavaMethodObject> getGenHandles() {
		return PythonObject.sfields;
	}
}
