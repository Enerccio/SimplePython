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
package me.enerccio.sp.interpret;

import java.util.Map;
import java.util.Set;

import me.enerccio.sp.runtime.ModuleInfo;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.JavaMethodObject;

/**
 * Represents root exception that can be raised by SimplePython
 * @author Enerccio
 *
 */
public class PythonException extends RuntimeException {
	private static final long serialVersionUID = 1446541378354L;
	
	public PythonException(String string) {
		super(string);
	}
	
	public PythonException(){
		super();
	}
	

	public static final StackElement SYSTEM_FRAME = new StackElement(); 
	public static final StackElement LAST_FRAME = new StackElement();

	/** Container for stack data */
	public static class StackElement extends PythonObject {
		private static final long serialVersionUID = -3288411350030175582L;
		public final ModuleInfo module;
		public final int line;
		public final int character;
		public final String function;
		
		private StackElement() {
			line = character = -1;
			module = null;
			function = "<module>";
		}

		public StackElement(ModuleInfo module, String function, int line, int character) {
			this.module = module;
			this.line = line;
			this.character = character;
			this.function = function;
		}

		@Override
		public boolean truthValue() {
			return true;
		}

		@Override
		protected String doToString() {
			return "<" + module.getName() + " at line " + line + " " + character + ">"; 
		}
		
		@Override
		public Set<String> getGenHandleNames() {
			return PythonObject.sfields.keySet();
		}

		@Override
		protected Map<String, JavaMethodObject> getGenHandles() {
			return PythonObject.sfields;
		}
	}

}
