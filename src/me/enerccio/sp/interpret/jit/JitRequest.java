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
	}

	private CompiledBlockObject block;
	private ClassLoader loader;
	private int pcFrom;
	private boolean debug;
	
	public volatile boolean hasResult = false;
	public volatile int nextPc;
	public volatile CompiledPython result;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((block == null) ? 0 : block.hashCode());
		result = prime * result + ((loader == null) ? 0 : loader.hashCode());
		result = prime * result + pcFrom;
		result = prime * result
				+ ((this.result == null) ? 0 : this.result.hashCode());
		result = prime * result
				+ ((this.debug) ? 0 : 1);
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
		if (block == null) {
			if (other.block != null)
				return false;
		} else if (!block.equals(other.block))
			return false;
		if (loader == null) {
			if (other.loader != null)
				return false;
		} else if (!loader.equals(other.loader))
			return false;
		if (pcFrom != other.pcFrom)
			return false;
		if (debug != other.debug)
			return false;
		if (result == null) {
			if (other.result != null)
				return false;
		} else if (!result.equals(other.result))
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
	public void setBlock(CompiledBlockObject block) {
		this.block = block;
	}
	public ClassLoader getLoader() {
		return loader;
	}
	public void setLoader(ClassLoader loader) {
		this.loader = loader;
	}
	public int getPcFrom() {
		return pcFrom;
	}
	public void setPcFrom(int pcFrom) {
		this.pcFrom = pcFrom;
	}
	public CompiledPython getResult() {
		return result;
	}
	public void setResult(CompiledPython result) {
		this.result = result;
	}
}
