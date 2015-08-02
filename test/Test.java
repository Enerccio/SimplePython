import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.enerccio.sp.SimplePython;
import me.enerccio.sp.interpret.PythonPathResolver;


public class Test {
	
	public static void main(String[] args) throws Exception {
		
		long c = System.currentTimeMillis();
		long c2 = 0;
		
		try {
			SimplePython.initialize();
			SimplePython.setAllowAutowraps(true);
			SimplePython.addResolve(PythonPathResolver.make(Paths.get("").toAbsolutePath().toString() + File.separator + "bin"));
			
			SimplePython.getModule("x");
			c2 = System.currentTimeMillis();
			
			List<List<Integer>> ill = new ArrayList<List<Integer>>();
			for (int i=0; i<10; i++){
				List<Integer> ill2 = new ArrayList<Integer>();
				for (int j=0; j<10; j++)
					ill2.add(j);
				ill.add(ill2);
			}
			System.out.println(SimplePython.asTuple(ill));
			System.out.println(SimplePython.convertJava(ill));
			
			Map<String, Integer> mm = new HashMap<String, Integer>();
			for (int i=0; i<10; i++)
				mm.put(Integer.toBinaryString(i), i);
			System.out.println(SimplePython.convertJava(mm));
			
			SimplePython.executeFunction("x", "test");

		} finally {
			System.out.println("Took " + (System.currentTimeMillis() - c) + " ms");
			System.out.println("Took pure runtime " + (System.currentTimeMillis() - c2) + " ms");
		}
	}

}