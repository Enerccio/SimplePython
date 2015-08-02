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

import java.io.File;
import java.io.FileInputStream;

import me.enerccio.sp.runtime.ModuleProvider;
import me.enerccio.sp.utils.Utils;

/**
 * PythonPath resolver. This is standard disk python path resolver. You provide path and SP will search there for .spy files and packages
 * @author Enerccio
 *
 */
public class PythonPathResolver implements PythonDataSourceResolver {

	private PythonPathResolver(){
		
	}
	
	private File rootPath;
	
	@Override
	public ModuleProvider resolve(String name, String resolvePath) {
		String pp = resolvePath.replace(".", File.separator);
		File path = new File(new File(rootPath, pp), name + ".spy");
		if (!path.exists())
			path = new File(new File(rootPath, pp), name);
		if (path.exists()){
			if (path.isDirectory()){
				File init = new File(path, "__init__.spy");
				if (init.exists() && !init.isDirectory()){
					try {
						String fname = path.getName();
						return doResolve(init, fname, name, resolvePath, true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				try {
					String fname = path.getName();
					fname.replace(".spy", "");
					return doResolve(path, fname, name, resolvePath, false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	private ModuleProvider doResolve(File path, String name, String mname, String resolvePath, boolean isp) throws Exception {
		return new ModuleProvider(mname, path.getName(), 
				Utils.toByteArray(new FileInputStream(path)), 
				path.getParentFile().equals(rootPath) ? "" : (!resolvePath.equals("") ? (resolvePath + ".") : "") + path.getParentFile().getName(), isp);
	}

	public static PythonPathResolver make(String string) {
		PythonPathResolver p = new PythonPathResolver();
		
		p.rootPath = new File(string);
		
		return p;
	}

}
