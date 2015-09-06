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
package me.enerccio.sp.compiler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;

import me.enerccio.sp.compiler.BlockDefinition.DataTag;
import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.ModuleObject;
import me.enerccio.sp.types.ModuleObject.ModuleData;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.utils.Pair;

public class ModuleDefinition {

	private static final int pycHeader = 0xDEADBABE;
	private static final int version = 3;

	public ModuleDefinition(byte[] inputData) throws Exception {
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(
				inputData));
		int header = dis.readInt();
		if (header != pycHeader)
			throw new RuntimeException("unknown header " + header
					+ ", expected " + pycHeader);
		int ver = dis.readInt();
		if (version != ver)
			throw new RuntimeException("mismatched version");
		name = BlockDefinition.unpackTaggedData(dis);
		root = BlockDefinition.unpackTaggedData(dis);
	}

	public ModuleDefinition(ModuleObject mo) throws Exception {
		name = Pair.makePair(DataTag.STRING,
				(Object) mo.fields.get("__name__").object.toString());
		root = Pair.makePair(DataTag.MODULE, (Object) Pair.makePair(
				DataTag.BLOCK, (Object) new BlockDefinition(mo.getFrame())));
	}

	private Pair<DataTag, Object> root;
	private Pair<DataTag, Object> name;

	public void writeToStream(OutputStream os) throws Exception {
		if (os == null)
			return;
		DataOutputStream wr = new DataOutputStream(os);
		wr.writeInt(pycHeader);
		wr.writeInt(version);
		wr.write(BlockDefinition.asBytes(name, version));
		wr.write(BlockDefinition.asBytes(root, version));
		os.close();
	}

	@SuppressWarnings("unchecked")
	public ModuleObject toModule(ModuleData data) {
		ModuleObject mo = new ModuleObject(data);
		BlockDefinition b = (BlockDefinition) ((Pair<DataTag, Object>) root
				.getSecond()).getSecond();
		mo.frame = b.toFrame(data);
		mo.fields.put("__name__", new AugumentedPythonObject(new StringObject(
				(String) name.getSecond()), AccessRestrictions.PUBLIC));
		return mo;
	}
}
