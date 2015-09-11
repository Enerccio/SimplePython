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
package me.enerccio.sp.interpret.jit;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

import me.enerccio.sp.compiler.Bytecode;
import me.enerccio.sp.interpret.CompiledBlockObject;
import me.enerccio.sp.interpret.FrameObject;

public class JJITContainer {
	
	public JJITContainer(CompiledBlockObject block) {
		if (JJITCompiler.isEnabled()){
			schedule(block, 0, false);
			schedule(block, 0, true);
		}
	}

	private ConcurrentHashMap<Integer, JitRequest> requests = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Integer, JitRequest> requestsDebug = new ConcurrentHashMap<>();
	
	private synchronized void schedule(CompiledBlockObject block, int i, boolean debug) {
		JitRequest jr = new JitRequest(block, block.boundClassLoader, i, debug);
		if (debug)
			requestsDebug.put(i, jr);
		else
			requests.put(i, jr);
		JJITCompiler.enqueue(jr);
	}
	
	public void enschedule(CompiledBlockObject block, int i, boolean debug){
		if (!JJITCompiler.isEnabled())
			return;
		
		ByteBuffer bb = ByteBuffer.wrap(block.getBytedata());
		bb.position(i);
		
		Bytecode b = Bytecode.fromNumber(bb.get() & 0xFF);
		
		switch (b){
		case KCALL:
		case RCALL:
		case TRUTH_VALUE:
		case UNPACK_SEQUENCE:
		case CALL:
			enschedule(block, i+5, debug);
			break;
		case UNPACK_KWARG:
			enschedule(block, i+1, debug);
			break;
		case ACCEPT_ITER:
		case SETUP_LOOP:
		case GOTO:
		case JUMPIFFALSE:
		case JUMPIFNONE:
		case JUMPIFNORETURN:
		case JUMPIFTRUE:
		case PUSH_FRAME:
			int jp = bb.getInt();
			enschedule(block, i+5, debug);
			enschedule(block, jp, debug);
			break;
		case GET_ITER:
			jp = bb.getInt();
			enschedule(block, i+5, debug);
			enschedule(block, i+10, debug);
			enschedule(block, jp, debug);
			break;
		case RETURN:
		case RAISE:
		case YIELD:
			return;
		default: 
			schedule(block, i, debug);
			break;
		}
	}

	public CompiledPython load(FrameObject frame, CompiledBlockObject block, int pc, Bytecode b, boolean debug){
		
		if (!JJITCompiler.isEnabled())
			return null;
		
		switch (b){
		case TRUTH_VALUE:
		case UNPACK_KWARG:
		case UNPACK_SEQUENCE:
		case SETUP_LOOP:
		case ACCEPT_ITER:
		case GET_ITER:
		case GOTO:
		case JUMPIFFALSE:
		case JUMPIFNONE:
		case JUMPIFNORETURN:
		case JUMPIFTRUE:
		case CALL:
		case RETURN:
		case KCALL:
		case RCALL:
		case YIELD:
		case PUSH_FRAME:
		case RAISE:
			return null;
		default: 
			break;
		}
		
		JitRequest rq;
		if (!debug){
			if (!requests.containsKey(pc)){
				schedule(block, pc, debug);
			}
			rq = requests.get(pc);
		} else {
			if (!requestsDebug.containsKey(pc)){
				schedule(block, pc, debug);
			}
			rq = requestsDebug.get(pc);
		}
		
		while (!rq.hasResult)
			;
		
		frame.pc = rq.nextPc;
		return rq.result;
	}
}
