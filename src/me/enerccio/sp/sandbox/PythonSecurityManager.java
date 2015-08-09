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
package me.enerccio.sp.sandbox;

import me.enerccio.sp.utils.Utils;

public abstract class PythonSecurityManager {
	
	public static final PythonSecurityManager DISABLE_ALL = new PythonSecurityManager(){

		@Override
		public boolean actionAllowed(SecureAction a,
				Object... additionalDeciders) {
			return false;
		}
		
	};

	public enum SecureAction {
		OPEN_FILE, JAVA_INSTANCE_CREATION, NEW_THREAD, TERMINATE_JAVA,
		DISASSEMBLY, RUNTIME_EVAL, RUNTIME_COMPILE
	}
	
	public void checkSandbox(SecureAction a, String callName, Object... additionalDeciders){
		if (!actionAllowed(a, additionalDeciders)){
			throw Utils.throwException("SandboxViolationError", callName+"(): not allowed");
		}
	}
	
	public abstract boolean actionAllowed(SecureAction a, Object... additionalDeciders);
}
