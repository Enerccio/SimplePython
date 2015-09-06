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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.ModuleObject.ModuleData;

/**
 * PythonPath resolver. This is standard disk python path resolver. You provide
 * path and SP will search there for .spy files and packages
 * 
 * @author Enerccio
 * @see FilesystemResolver#make(String)
 */
public class FilesystemResolver implements ModuleResolver {
	private static final long serialVersionUID = 8811099800748652767L;
	private static final String RESOLVER_ID = "FSPR_1";
	private File rootPath;

	public FilesystemResolver(String path) {
		rootPath = new File(path);
	}

	@Override
	public ModuleData resolve(String name, String resolvePath) {
		String pp = resolvePath.replace(".", File.separator);
		File path = new File(new File(rootPath, pp), name + ".py");
		File jpath = new File(new File(rootPath, pp), name + ".pyj");
		if (!path.exists())
			path = new File(new File(rootPath, pp), name);
		if (path.exists()) {
			if (path.isDirectory()) {
				File init = new File(path, "__init__.py");
				if (init.exists() && !init.isDirectory()) {
					try {
						return new MI(name, path, resolvePath, true, false);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				File jinit = new File(path, "__init__.pyj");
				if (jinit.exists() && !jinit.isDirectory()) {
					try {
						return new MI(name, path, resolvePath, true, true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				try {
					if (path.getName().endsWith(".py")) {
						return new MI(name, path, resolvePath, true, false);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if (jpath.exists()) {
			try {
				if (jpath.isFile()) {
					return new MI(name, jpath, resolvePath, true, true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public InputStream read(ModuleData data) throws IOException {
		return new FileInputStream(((MI) data).file);
	}

	@Override
	public long lastModified(ModuleData data) {
		return ((MI) data).file.lastModified();
	}

	private class MI implements ModuleData {
		private static final long serialVersionUID = -5150375199772148611L;
		private String name;
		private File file;
		private String resolvePath;
		private boolean isPackage;
		private boolean isJava;

		MI(String name, File file, String resolvePath, boolean isPackage,
				boolean isJava) {
			this.name = name;
			this.file = file;
			this.resolvePath = resolvePath;
			this.isPackage = isPackage;
			this.isJava = isJava;
		}

		@Override
		public ModuleResolver getResolver() {
			return FilesystemResolver.this;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getFileName() {
			return file.getName();
		}

		@Override
		public String getPackageResolve() {
			return resolvePath;
		}

		@Override
		public boolean isPackage() {
			return isPackage;
		}

		@Override
		public boolean isJavaClass() {
			return isJava;
		}
	}

	@Override
	public InputStream cachedRead(ModuleData data) {
		return PythonRuntime.cachedRead(data);
	}

	@Override
	public OutputStream cachedWrite(ModuleData data) {
		return PythonRuntime.cachedWrite(data);
	}

	@Override
	public String getResolverID() {
		return RESOLVER_ID + rootPath.getAbsolutePath();
	}
}
