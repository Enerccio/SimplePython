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

import me.enerccio.sp.runtime.ModuleProvider;

/**
 * This interface is for customizing where will python look for resolving imports
 * @author Enerccio
 *
 */
public interface PythonDataSourceResolver {

	/**
	 * Constructs module provider for the requested name and requested path, or null if name does not corresponds to any real module
	 * @param name
	 * @param resolvePath
	 * @return null or ModuleProvider
	 */
	ModuleProvider resolve(String name, String resolvePath);
	
}
