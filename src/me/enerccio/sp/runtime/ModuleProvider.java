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

/**
 * ModuleProvider represents module file into memory
 * @author Enerccio
 *
 */
public class ModuleProvider {
	
	public ModuleProvider(String moduleName, String srcFile, byte[] source, String packageResolve, boolean isPackage){
		this.moduleName = moduleName;
		this.packageResolve = packageResolve;
		this.source = source;
		this.srcFile = srcFile;
		this.isPackage = isPackage;
	}
	
	/**
	 * @return name of the module
	 */
	public String getModuleName() {
		return moduleName;
	}

	/**
	 * @return input stream of the source
	 */
	public InputStream getSource() {
		return new ByteArrayInputStream(source);
	}

	/** returns name of the source file */
	public String getSrcFile() {
		return srcFile;
	}

	/**
	 * returns package resolve
	 * package resolve is local name of the previous package chain. 
	 * @return
	 */
	public String getPackageResolve() {
		return packageResolve;
	}
	
	public boolean isPackage() {
		return isPackage;
	}

	private final String moduleName;
	private final byte[] source;
	private final String srcFile;
	private final String packageResolve;
	private final boolean isPackage;

}
