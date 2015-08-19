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
import java.io.OutputStream;

/**
 * ModuleProvider represents module file into memory
 * @author Enerccio
 *
 */
public class ModuleProvider {
	
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
		if (source == null)
			return null;
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

	private final String 	   moduleName;
	private final byte[] 	   source;
	private final String 	   srcFile;
	private final String 	   packageResolve;
	private final boolean 	   isPackage;
	private final boolean	   isPrecompiled;
	private final boolean 	   allowPrecompilation;
	private final OutputStream precompilationTarget;
	private final byte[]	   compiledSource;
	
	public ModuleProvider(String moduleName, byte[] source, String srcFile,
			String packageResolve, boolean isPackage, boolean isPrecompiled,
			boolean allowPrecompilation, OutputStream precompilationTarget, byte[] compiledSource) {
		super();
		this.moduleName = moduleName;
		this.source = source;
		this.srcFile = srcFile;
		this.packageResolve = packageResolve;
		this.isPackage = isPackage;
		this.isPrecompiled = isPrecompiled;
		this.allowPrecompilation = allowPrecompilation;
		this.precompilationTarget = precompilationTarget;
		this.compiledSource = compiledSource;
	}

	public boolean isPrecompiled() {
		return isPrecompiled;
	}

	public boolean isAllowPrecompilation() {
		return allowPrecompilation;
	}

	public OutputStream getPrecompilationTarget() {
		return precompilationTarget;
	}

	public byte[] getCompiledSource() {
		return compiledSource;
	}
	
	

}
