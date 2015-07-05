package me.enerccio.sp.runtime;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModuleProvider {
	
	public ModuleProvider(String moduleName, String srcFile, byte[] source, String... packages){
		this.moduleName = moduleName;
		this.packages.addAll(Arrays.asList(packages));
		this.source = source;
		this.srcFile = srcFile;
	}
	
	public String getModuleName() {
		return moduleName;
	}

	public List<String> getPackages() {
		return new ArrayList<String>(packages);
	}

	public InputStream getSource() {
		return new ByteArrayInputStream(source);
	}

	public String getSrcFile() {
		return srcFile;
	}

	private final String moduleName;
	private final byte[] source;
	private final String srcFile;
	private final List<String> packages = new ArrayList<String>();

}
