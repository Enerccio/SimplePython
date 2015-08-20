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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import me.enerccio.sp.types.ModuleObject.ModuleData;

/**
 * This interface is for customizing where will python look for resolving imports
 * @author Enerccio
 *
 */
public interface ModuleResolver {
	/**
	 * Constructs module data for the requested name and requested path.
	 * Returns null if name does not corresponds to any module.
	 * 
	 * This may be called repeatedly to resolve module in nested packages.
	 * For example, to get module 'utils.scripts.protocols.http', following
	 * sequence will be called:
	 * 
	 * utils     = resolve("utils", "");
	 * scripts   = resolve("scripts", utils.getResolvePath());
	 * protocols = resolve("protocols", scripts.getResolvePath());
	 * http      = resolve("http", protocols.getResolvePath());
	 * 
	 * assuming that utils, scripts and protocols are valid packages.
	 * 
	 * @param name module name without package nor any filename extension
	 * @param resolvePath package that above name is relative to
	 * @return null or ModuleData
	 */
	ModuleData resolve(String name, String resolvePath);

	
	InputStream read(ModuleData data) throws IOException;

	/**
	 * Returns last modification time of module.
	 * Use System.MAX_LONG if nothing better is available.
	 */
	long lastModified(ModuleData data);

	/** 
	 * Returns input stream for cached (compiled) module or null if cache is not supported or available.
	 * Use PythonRuntime.cachedRead method to use default caching method.
	 */
	InputStream cachedRead(ModuleData data);
	
	/** 
	 * Returns output stream to write compiled module to or null if cache is not supported.
	 * Use PythonRuntime.cachedRead method to use default caching method.
	 */
	OutputStream cachedWrite(ModuleData data);

	/** Returns any random string that should be same as long as resolver works in same way */
	String getResolverID();
	
}
