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

import java.io.InputStream;

import me.enerccio.sp.runtime.ModuleProvider;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.utils.Utils;

/**
 * PathResolver which searches root of the jar/java path for .spy.
 * @author Enerccio
 *
 */
public class InternalJavaPathResolver implements PythonDataSourceResolver {

	public InternalJavaPathResolver(){
		
	}
	
	@Override
	public ModuleProvider resolve(String name, String resolvePath) {
		if (name.contains("."))
			return null;
		InputStream is = PythonRuntime.runtime.getClass().getClassLoader().getResourceAsStream(name + ".py");
		try {
			return doResolve(is, name+".py", name);
		} catch (Exception e) {
			return null;
		}
	}

	private ModuleProvider doResolve(InputStream is, String fname, String mname) throws Exception {
		if (is == null) return null;
		return new ModuleProvider(mname, fname, Utils.toByteArray(is), "", false);
	}

}
