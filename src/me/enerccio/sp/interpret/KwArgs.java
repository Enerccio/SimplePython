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

import java.util.HashMap;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.utils.Utils;

public interface KwArgs {
	
	/** 
	 * Removes argument from list and returns its value. 
	 * Returns null if argument is not found
	 */
	public PythonObject consume(String arg);

	/** 
	 * Removes argument from list and returns its value as String. 
	 * Returns null if argument is not found.
	 */
	public String consumeString(String arg);
	
	/** 
	 * Check if kwarg list is empty and throws exception if not.
	 * Passed name is used as function name in error message.  
	 */
	public void checkEmpty(String arg);

	/** 
	 * Throws exception if there is any kwarg in list.  
	 */
	public void notExpectingKWArgs();

	/**
	 * Returns true if argument is defined
	 */
	public boolean contains(String string);


	static class HashMapKWArgs extends HashMap<String, PythonObject>  implements KwArgs {
		private static final long serialVersionUID = 7370108455070437208L;

		@Override
		public PythonObject consume(String arg) {
			return remove(arg);
		}

		@Override
		public String consumeString(String arg) {
			if (containsKey(arg))
				return remove(arg).toString();
			return null;
		}

		@Override
		public void checkEmpty(String name) {
			if (size() == 0)
				return;
			String key = keySet().iterator().next();
			throw Utils.throwException("TypeError", name + " got an unexpected keyword argument '" + key + "'");
		}

		@Override
		public void notExpectingKWArgs() {
			checkEmpty("function");
		}

		@Override
		public boolean contains(String arg) {
			return containsKey(arg);
		}
	}
}