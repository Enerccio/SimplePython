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

import me.enerccio.sp.interpret.CompiledBlockObject;

public class JitRequest {

	public JitRequest(CompiledBlockObject block, ClassLoader loader, int pcFrom, boolean debug) {
		super();
		this.block = block;
		this.loader = loader;
		this.pcFrom = pcFrom;
		this.debug = debug;
		this.blockId = block.hashCode();
	}

	private CompiledBlockObject block;
	private ClassLoader loader;
	private int pcFrom;
	private boolean debug;
	private int blockId;
	
	public volatile boolean hasResult = false;
	public volatile int nextPc;
	public volatile CompiledPython result;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + blockId;
		result = prime * result + (debug ? 1231 : 1237);
		result = prime * result + pcFrom;
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
		JitRequest other = (JitRequest) obj;
		if (blockId != other.blockId)
			return false;
		if (debug != other.debug)
			return false;
		if (pcFrom != other.pcFrom)
			return false;
		return true;
	}
	public void setDebug(boolean debug){
		this.debug = debug;
	}
	public boolean getDebug(){
		return debug;
	}
	public CompiledBlockObject getBlock() {
		return block;
	}
	public ClassLoader getLoader() {
		return loader;
	}
	public int getPcFrom() {
		return pcFrom;
	}
	public CompiledPython getResult() {
		return result;
	}
	public void setResult(CompiledPython result) {
		this.result = result;
	}
}
