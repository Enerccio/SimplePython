package me.enerccio.sp.runtime;

import java.io.IOException;
import java.io.InputStream;

import org.jcp.xml.dsig.internal.dom.Utils;

public class PythonClassLoader extends ClassLoader {

	public PythonClassLoader(ClassLoader parent){
		super(parent);
	}
	
	public Class<?> load(String binaryName, InputStream inputData) throws IOException{
		byte[] array = Utils.readBytesFromStream(inputData);
		return defineClass(binaryName, array, 0, array.length);
	}
}
