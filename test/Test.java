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
import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;

import me.enerccio.sp.SimplePython;
import me.enerccio.sp.interpret.PythonPathResolver;
import me.enerccio.sp.sandbox.PythonSecurityManager;


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
			
			SimplePython.executeFunction("x", "test");

		} finally {
			System.out.println("Took " + (System.currentTimeMillis() - c) + " ms");
			System.out.println("Took pure runtime " + (System.currentTimeMillis() - c2) + " ms");
		}
	}

}