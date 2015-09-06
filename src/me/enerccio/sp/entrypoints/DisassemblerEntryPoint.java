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
package me.enerccio.sp.entrypoints;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import me.enerccio.sp.SimplePython;
import me.enerccio.sp.compiler.ModuleDefinition;
import me.enerccio.sp.types.ModuleObject;
import me.enerccio.sp.utils.StaticTools.IOUtils;

public class DisassemblerEntryPoint {

	public static void main(String[] args) throws Exception {
		SimplePython.initialize();

		String input = args[0];
		String output = args[1];

		File in = new File(input);
		File out = new File(output);

		disassemble(in, out);
	}

	public static void disassemble(File in, File out)
			throws FileNotFoundException, IOException, Exception {
		ModuleObject mo = new ModuleDefinition(
				IOUtils.toByteArray(new FileInputStream(in))).toModule(null);
		mo.disassembleToStream(new FileOutputStream(out), in);
	}

}
