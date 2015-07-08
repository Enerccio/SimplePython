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
