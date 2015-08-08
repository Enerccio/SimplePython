import java.io.File;
import java.nio.file.Paths;

import me.enerccio.sp.SimplePython;
import me.enerccio.sp.interpret.PythonPathResolver;
import me.enerccio.sp.types.ModuleObject;

public class Test {
	
	public static void main(String[] args) throws Exception {
		
		long c = System.currentTimeMillis();
		long c2 = 0;
		
		try {
			SimplePython.initialize();
			SimplePython.setAllowAutowraps(true);
			SimplePython.addResolver(PythonPathResolver.make(Paths.get("").toAbsolutePath().toString() + File.separator + "bin"));
			
			ModuleObject x = SimplePython.getModule("x");
			c2 = System.currentTimeMillis();
			if (x.getField("test") != null)
				SimplePython.executeFunction("x", "test");

		} finally {
			System.out.println("Took " + (System.currentTimeMillis() - c) + " ms");
			System.out.println("Took pure runtime " + (System.currentTimeMillis() - c2) + " ms");
		}
	}

}