import java.io.File;
import java.nio.file.Paths;

import me.enerccio.sp.interpret.ExecutionResult;
import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.interpret.PythonPathResolver;
import me.enerccio.sp.runtime.PythonRuntime;


public class Test {
	
	public static void main(String[] args) throws Exception {
		final PythonRuntime r = PythonRuntime.runtime;
		r.addResolver(PythonPathResolver.make(Paths.get("").toAbsolutePath().toString() + File.separator + "bin"));
		
		PythonInterpret i = PythonInterpret.interpret.get();
		r.getRoot("x");
		
		while (i.executeOnce() == ExecutionResult.OK)
			;
	}

}