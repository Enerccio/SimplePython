import java.io.File;
import java.nio.file.Paths;

import me.enerccio.sp.SimplePython;
import me.enerccio.sp.interpret.FilesystemResolver;
import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.interpret.CompiledBlockObject.DebugInformation;
import me.enerccio.sp.interpreter.debug.AbstractDebugger;
import me.enerccio.sp.serialization.XMLPySerializer;
import me.enerccio.sp.types.ModuleObject;

public class Test {
	
	public static void main(String[] args) throws Exception {
		
		long c = System.currentTimeMillis();
		long c2 = 0;
		
		try {
			File cache = new File("__pycache__");
			cache.mkdir();
			SimplePython.addPycCache(cache);
			
			SimplePython.initialize();
			SimplePython.setAllowAutowraps(true);
			SimplePython.addResolver(new FilesystemResolver(Paths.get("").toAbsolutePath().toString() + File.separator + "bin" + File.separator + "t"));
			
			Thread t = new Thread(){

				@Override
				public void run() {
					try {
						Thread.sleep(500);
						SimplePython.serialize(new XMLPySerializer(new File("ser.xml")));
					} catch (Exception e){
						e.printStackTrace();
					}
				}
				
			};
			t.start();
			
			PythonInterpreter i = PythonInterpreter.interpreter.get();
			final AbstractDebugger d = new AbstractDebugger();
			
			i.attachDebugger(d);
			d.installBreakpoint("x", "", 14);
			
			t = new Thread(){

				@Override
				public void run() {
					while (true){
						try {
							d.breakingSemaphore.acquire();
						} catch (InterruptedException e) {
							return;
						}
						
						System.err.println("Debugging " + d.f + " for " + d.i + " - cpc: " + d.cpc + ", opcode " + d.cbc);
						DebugInformation di = d.f.getCompiled().getDebugInformation(d.cpc);
						System.err.println("Debugging: " + di.function + " at module " + di.module.getName() + ", lineno: " + di.lineno);
						d.breakNextLine();
						
						d.waitingSemaphore.release();
					}
				}
				
			};
			t.setDaemon(true);
			t.start();
			
			ModuleObject x = SimplePython.getModule("x");
			c2 = System.currentTimeMillis();
			if (x.getField("test") != null)
				SimplePython.executeFunction("x", "test");

		} finally {
			System.out.println();
			System.out.println("Runtime statistics:");
			System.out.println("Took total " + (System.currentTimeMillis() - c) + " ms");
			System.out.println("Took init " + ((System.currentTimeMillis() - c) - (System.currentTimeMillis() - c2)) + " ms");
			System.out.println("Took pure runtime " + (System.currentTimeMillis() - c2) + " ms");
		}
	}

}