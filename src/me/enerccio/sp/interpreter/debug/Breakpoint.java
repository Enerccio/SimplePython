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
package me.enerccio.sp.interpreter.debug;

import me.enerccio.sp.interpret.CompiledBlockObject.DebugInformation;

public class Breakpoint {

	private String moduleName;
	private String modulePath;
	private int line;
	boolean appliedRecently;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + line;
		result = prime * result
				+ ((moduleName == null) ? 0 : moduleName.hashCode());
		result = prime * result
				+ ((modulePath == null) ? 0 : modulePath.hashCode());
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
		Breakpoint other = (Breakpoint) obj;
		if (line != other.line)
			return false;
		if (moduleName == null) {
			if (other.moduleName != null)
				return false;
		} else if (!moduleName.equals(other.moduleName))
			return false;
		if (modulePath == null) {
			if (other.modulePath != null)
				return false;
		} else if (!modulePath.equals(other.modulePath))
			return false;
		return true;
	}

	Breakpoint(String moduleName, String modulePath, int line){
		this.moduleName = moduleName;
		this.modulePath = modulePath;
		this.line = line;
	}
	
	public boolean applies(DebugInformation debugInfo){
		return debugInfo.module.getPackageResolve().equals(modulePath) &&
			   debugInfo.module.getName().equals(moduleName) &&
			   debugInfo.lineno == line;
	}

	public void doesNotApply() {
		appliedRecently = false;
	}
	
	public void doesApply(){
		appliedRecently = true;
	}
	
}
