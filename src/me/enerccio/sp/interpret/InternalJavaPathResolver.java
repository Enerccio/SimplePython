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
import java.net.URL;

import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.ModuleObject.ModuleData;

/**
 * PathResolver which searches root of the jar/java path for .spy.
 * 
 * @author Enerccio
 *
 */
public class InternalJavaPathResolver implements ModuleResolver {
	private static final long serialVersionUID = -7378642828100764595L;
	private static final String RESOLVER_ID = "IJPR_1";

	@Override
	public ModuleData resolve(String name, String resolvePath) {
		if (name.contains("."))
			return null;
		try {
			InputStream is = PythonRuntime.runtime.getClass().getClassLoader()
					.getResourceAsStream(name + ".py");
			if (is == null)
				return null;
			return new MI(name, is);
		} catch (Exception e2) {
			return null;
		}
	}

	@Override
	public InputStream read(ModuleData data) {
		if (data instanceof MI)
			if (((MI) data).is != null)
				return ((MI) data).is;
		return getClass().getClassLoader().getResourceAsStream(
				data.getFileName());
	}

	@Override
	public long lastModified(ModuleData data) {
		URL url = getClass().getResource("/" + data.getFileName());
		long l;
		try {
			l = url.openConnection().getLastModified();
			return (l == 0) ? Long.MAX_VALUE : l;
		} catch (IOException e) {
		}
		return Long.MAX_VALUE;
	}

	public ModuleData getModuleData(String name) {
		return new MI(name);
	}

	private class MI implements ModuleData {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7371254199796731185L;
		private String name;
		private transient InputStream is;

		MI(String name) {
			this.name = name;
			this.is = null;
		}

		MI(String name, InputStream is) {
			this.name = name;
			this.is = is;
		}

		@Override
		public ModuleResolver getResolver() {
			return InternalJavaPathResolver.this;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getFileName() {
			return name + ".py";
		}

		@Override
		public String getPackageResolve() {
			return "";
		}

		@Override
		public boolean isPackage() {
			return false;
		}

		@Override
		public boolean isJavaClass() {
			return false;
		}
	}

	@Override
	public InputStream cachedRead(ModuleData data) {
		InputStream is = PythonRuntime.runtime.getClass().getClassLoader()
				.getResourceAsStream(data.getFileName() + "c");
		if (is == null)
			return PythonRuntime.cachedRead(data);
		return is;
	}

	@Override
	public OutputStream cachedWrite(ModuleData data) {
		return PythonRuntime.cachedWrite(data);
	}

	@Override
	public String getResolverID() {
		return RESOLVER_ID;
	}
}
