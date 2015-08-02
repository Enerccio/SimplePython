import java.io.File;
import java.nio.file.Paths;

import me.enerccio.sp.interpret.ExecutionResult;
import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.interpret.PythonPathResolver;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.ModuleObject;
import me.enerccio.sp.types.callables.UserFunctionObject;
import me.enerccio.sp.types.sequences.TupleObject;


public class Test {
	
	public static void main(String[] args) throws Exception {
		long c = System.currentTimeMillis();
		long c2 = 0;
		ExecutionResult rr;
		
		try {
			final PythonRuntime r = PythonRuntime.runtime;
			r.setAllowAutowraps(true);
			r.addResolver(PythonPathResolver.make(Paths.get("").toAbsolutePath().toString() + File.separator + "bin"));
			
			PythonInterpreter i = PythonInterpreter.interpret.get();
			ModuleObject mo = r.getRoot("x");
			
			c2 = System.currentTimeMillis();
			
			UserFunctionObject fo = (UserFunctionObject) mo.getField("test");
			if (fo != null)
				fo.call(new TupleObject(), null);
			while (true){
				rr = i.executeOnce();
				if (rr == ExecutionResult.EOF || rr == ExecutionResult.FINISHED)
					if (i.currentFrame.size() == 0)
						break;
			}

		} finally {
			System.out.println("Took " + (System.currentTimeMillis() - c) + " ms");
			System.out.println("Took pure runtime " + (System.currentTimeMillis() - c2) + " ms");
		}
	}

}