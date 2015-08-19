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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import me.enerccio.sp.runtime.ModuleProvider;
import me.enerccio.sp.utils.StaticTools.IOUtils;

/**
 * PythonPath resolver. This is standard disk python path resolver. You provide path and SP will search there for .spy files and packages
 * @author Enerccio
 * @see PythonPathResolver#make(String)
 */
public class PythonPathResolver implements PythonDataSourceResolver {

	private PythonPathResolver(){
		
	}
	
	private File rootPath;
	
	@Override
	public ModuleProvider resolve(String name, String resolvePath) {
		String pp = resolvePath.replace(".", File.separator);
		File path = new File(new File(rootPath, pp), name + ".py");
		if (!path.exists())
			path = new File(new File(rootPath, pp), name + ".pyc");
		if (!path.exists())
			path = new File(new File(rootPath, pp), name);
		if (path.exists()){
			if (path.isDirectory()){
				File init = new File(path, "__init__.py");
				if (init.exists() && !init.isDirectory()){
					try {
						String fname = path.getName();
						return doResolveNoPyc(init, fname, name, resolvePath, true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				File initc = new File(path, "__init__.pyc");
				if (initc.exists() && !initc.isDirectory()){
					try {
						String fname = path.getName();
						return doResolveOnlyPyc(initc, fname, name, resolvePath, true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				try {
					String fname = path.getName();
					if (fname.endsWith(".py")){
						fname = fname.replace(".py", "");
						return doResolveNoPyc(path, fname, name, resolvePath, false);
					} else {
						fname = fname.replace(".pyc", "");
						return doResolveOnlyPyc(path, fname, name, resolvePath, false);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	private ModuleProvider doResolveOnlyPyc(File path, String name, String mname, String resolvePath, boolean isp) throws Exception {
		
		return new ModuleProvider(mname, null, path.getName(),  
				path.getParentFile().equals(rootPath) ? "" : (!resolvePath.equals("") ? (resolvePath + ".") : "") + path.getParentFile().getName(), isp,
				true, false, null, IOUtils.toByteArray(new FileInputStream(path)));
	}

	private ModuleProvider doResolveNoPyc(File path, String name, String mname, String resolvePath, boolean isp) throws Exception {
		boolean hasPyc = true;
		byte[] pycData = null;
		try {
			pycData = getPyc(path);
			
			String fp = path.getAbsolutePath();
			fp = fp.substring(0, fp.lastIndexOf("."));
			fp += ".pyc";
			File ff = new File(fp);
			if (ff.lastModified() < path.lastModified())
				hasPyc = false;
		} catch (Exception e){
			hasPyc = false;
		}
		
		OutputStream pyc = null;
		boolean doPyc = true;
		try {
			pyc = asPyc(path);
		} catch (Exception e){
			// can't open pyc output, ignore pyc
			doPyc = false;
		}
		
		return new ModuleProvider(mname, IOUtils.toByteArray(new FileInputStream(path)), path.getName(),  
				path.getParentFile().equals(rootPath) ? "" : (!resolvePath.equals("") ? (resolvePath + ".") : "") + path.getParentFile().getName(), isp,
				hasPyc, doPyc, pyc, pycData);
	}

	private byte[] getPyc(File path) throws FileNotFoundException, IOException {
		String fp = path.getAbsolutePath();
		fp = fp.substring(0, fp.lastIndexOf("."));
		fp += ".pyc";
		File ff = new File(fp);
		return IOUtils.toByteArray(new FileInputStream(ff));
	}

	private OutputStream asPyc(File path) throws Exception {
		String fp = path.getAbsolutePath();
		fp = fp.substring(0, fp.lastIndexOf("."));
		fp += ".pyc";
		File ff = new File(fp);
		return new FileOutputStream(ff);
	}

	/**
	 * Base factory for python path resolver. 
	 * @param string Absolute path 
	 * @return PythonPathResolver corresponding to this path
	 */
	public static PythonPathResolver make(String string) {
		PythonPathResolver p = new PythonPathResolver();
		
		p.rootPath = new File(string);
		
		return p;
	}

}
