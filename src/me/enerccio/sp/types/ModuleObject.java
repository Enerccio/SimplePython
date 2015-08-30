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
package me.enerccio.sp.types;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import me.enerccio.sp.compiler.PythonCompiler;
import me.enerccio.sp.errors.AttributeError;
import me.enerccio.sp.errors.SyntaxError;
import me.enerccio.sp.interpret.CompiledBlockObject;
import me.enerccio.sp.interpret.FrameObject;
import me.enerccio.sp.interpret.InternalDict;
import me.enerccio.sp.interpret.ModuleResolver;
import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.parser.pythonLexer;
import me.enerccio.sp.parser.pythonParser;
import me.enerccio.sp.parser.pythonParser.File_inputContext;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.serialization.PySerializer;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.callables.UserFunctionObject;
import me.enerccio.sp.types.mappings.StringDictObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.utils.StaticTools.ParserGenerator;
import me.enerccio.sp.utils.Ref;
import me.enerccio.sp.utils.Utils;

/**
 * Python object representing module
 * 
 * @author Enerccio
 *
 */
public class ModuleObject extends PythonObject {
	private static final long serialVersionUID = -2347220852204272570L;
	public static final String __NAME__ = "__name__";
	public static final String __DICT__ = "__dict__";
	public static final String __THISMODULE__ = "__thismodule__";
	private StringDictObject globals;

	/** module data for this module */
	public final ModuleData data;
	/** bytecode of the body of this module */
	public CompiledBlockObject frame;
	/** whether this module is inited or not */
	public volatile boolean isInited = false;

	@Override
	protected void serializeDirectState(PySerializer pySerializer) {
		pySerializer.serialize(isInited);
		pySerializer.serialize(frame);
		pySerializer.serializeJava(data);
		pySerializer.serializeJava(globals);
	}

	public ModuleObject(ModuleData data, boolean compilingBT) {
		super(false);
		this.data = data;

		Utils.putPublic(this, __NAME__, new StringObject(data.getName()));

		try {
			Ref<pythonLexer> r = new Ref<pythonLexer>();
			pythonParser p = ParserGenerator.parse(data, r);
			File_inputContext fcx = p.file_input();
			if (fcx != null) {
				frame = new PythonCompiler(r)
						.doCompile(fcx, data, compilingBT ? null
								: PythonRuntime.runtime.getGlobals());
			}
		} catch (Exception e) {
			throw new SyntaxError("failed to parse source code of "
					+ data.getFileName(), e);
		}
	}

	public ModuleObject(ModuleData data) {
		super(false);
		this.data = data;
	}

	@Override
	public boolean truthValue() {
		return true;
	}

	public ModuleData getModuleData() {
		return data;
	}

	public void injectGlobal(String key, PythonObject value) {
		globals.put(key, value);
	}

	@Override
	public PythonObject set(String key, PythonObject localContext,
			PythonObject value) {
		if (key.equals(__NAME__) || key.equals(__DICT__))
			throw new AttributeError("'"
					+ Utils.run("str", Utils.run("typename", this))
					+ "' object attribute '" + key + "' is read only");
		if (fields.containsKey(key))
			return super.set(key, localContext, value);
		else {
			if (!globals.contains(key))
				throw new AttributeError("'"
						+ Utils.run("str", Utils.run("typename", this))
						+ "' object has no attribute '" + key + "'");
			if (value == null)
				globals.backingMap.remove(key);
			else
				globals.put(key, value);
		}
		return NoneObject.NONE;
	}

	@Override
	public synchronized PythonObject get(String key, PythonObject localContext) {
		PythonObject o = super.get(key, localContext);
		if (o == null)
			o = globals.doGet(key);
		return o;
	}

	@Override
	protected String doToString() {
		return "<Module " + get(__NAME__, this).toString() + " at 0x"
				+ Integer.toHexString(hashCode()) + ">";
	}

	/**
	 * Initializes the module.
	 */
	public void initModule() {
		doInitModule();
		isInited = true;
	}

	/**
	 * Initializes the module by executing it's bytecode
	 */
	private void doInitModule() {
		int cfc = PythonInterpreter.interpreter.get().currentFrame.size();
		PythonInterpreter.interpreter.get().executeBytecode(frame);

		FrameObject newFrame = PythonInterpreter.interpreter.get().currentFrame
				.getLast();

		InternalDict args = new StringDictObject();
		args.putVariable(__THISMODULE__, this);
		args.putVariable(__NAME__, new StringObject(data.getName()));

		PythonInterpreter.interpreter.get().setArgs(args);

		PythonInterpreter.interpreter.get().executeAll(cfc);

		globals = (StringDictObject) newFrame.environment.getLocals();
		Utils.putPublic(this, __DICT__, globals);
	}

	/**
	 * Returns field from this module's dict (globals)
	 * 
	 * @param string
	 * @return
	 */
	public PythonObject getField(String string) {
		return globals.doGet(string);
	}

	@Override
	public Set<String> getGenHandleNames() {
		return PythonObject.sfields.keySet();
	}

	@Override
	protected Map<String, JavaMethodObject> getGenHandles() {
		return PythonObject.sfields;
	}

	public CompiledBlockObject getFrame() {
		return frame;
	}

	/** Provides informations about module */
	public static interface ModuleData extends Serializable {
		/**
		 * Returns module resolver used to load this module. May return null for
		 * some very special cases.
		 */
		ModuleResolver getResolver();

		/**
		 * Returns module name, including package, if possible. Used by trace
		 * and dis method.
		 */
		String getName();

		/**
		 * Returns module filename. For modules not generated from file, this
		 * may return same as getName()
		 */
		String getFileName();

		/**
		 * Returns weirdest shit...
		 */
		String getPackageResolve();

		/**
		 * Returns true if module is package
		 */
		boolean isPackage();
		
		/**
		 * Returns true if this module is actually hidden java
		 * @return
		 */
		boolean isJavaClass();
	}
	
	public void disassembleToStream(OutputStream out, File file) throws Exception {
		Set<CompiledBlockObject> dblocks = new HashSet<CompiledBlockObject>();
		Queue<CompiledBlockObject> blocks = new LinkedList<CompiledBlockObject>();  
		Queue<Object> parents = new LinkedList<Object>();
		
		blocks.add(frame);
		parents.add(this);
		
		BufferedWriter os = new BufferedWriter(new OutputStreamWriter(out, "utf-8"));
		
		try {
			os.write("Disassembly of the file " + file + "\n");
			
			while (!blocks.isEmpty()){
				CompiledBlockObject block = blocks.poll();
				dblocks.add(block);
				
				os.write(parents.poll().toString() + ":\n");
				os.write(CompiledBlockObject.dis(block, true));
				
				for (Integer key : block.mmap.keySet()){
					PythonObject po = block.mmap.get(key);
					if (po instanceof UserFunctionObject)
						if (!dblocks.contains(((UserFunctionObject)po).block)){
							blocks.add(((UserFunctionObject)po).block);
							parents.add(po);
						}
				}
				
				os.write("\n\n");
			}

		} finally {
			os.flush();
			os.close();
		}
	}
}
