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
package me.enerccio.sp.runtime;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ModuleProvider {
	
	public ModuleProvider(String moduleName, String srcFile, byte[] source, String packageResolve){
		this.moduleName = moduleName;
		this.packageResolve = packageResolve;
		this.source = source;
		this.srcFile = srcFile;
	}
	
	public String getModuleName() {
		return moduleName;
	}

	public InputStream getSource() {
		return new ByteArrayInputStream(source);
	}

	public String getSrcFile() {
		return srcFile;
	}

	public String getPackageResolve() {
		return packageResolve;
	}

	private final String moduleName;
	private final byte[] source;
	private final String srcFile;
	private final String packageResolve;

}
