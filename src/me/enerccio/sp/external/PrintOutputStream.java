package me.enerccio.sp.external;

import java.io.IOException;
import java.io.OutputStream;

import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.pointer.WrapAnnotationFactory.WrapMethod;
import me.enerccio.sp.utils.Utils;

public class PrintOutputStream {

	private boolean err;
	public PrintOutputStream(boolean err){
		this.err = err;
	}
	
	@WrapMethod
	public void write(String data){
		synchronized (PythonRuntime.runtime){
			@SuppressWarnings("resource")
			OutputStream os = err ? PythonRuntime.runtime.getErr() : PythonRuntime.runtime.getOut();
			try {
				os.write(data.getBytes());
			} catch (IOException e) {
				throw Utils.throwException("IOError", "failed to write to stream", e);
			}
		}
	}
	
	@Override
	public String toString(){
		return "std" + (err ? "err" : "out") + " stream";
	}
	
}
