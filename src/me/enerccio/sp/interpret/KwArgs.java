package me.enerccio.sp.interpret;

import java.util.HashMap;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.utils.Utils;

public class KwArgs extends HashMap<String, PythonObject> {
	private static final long serialVersionUID = 7370108455070437208L;

	/** 
	 * Removes argument from list and returns its value. 
	 * Returns null if argument is not found
	 */
	public PythonObject consume(String key) {
		return remove(key);
	}

	/** 
	 * Check if kwarg list is empty and throws exception if not.
	 * Passed name is used as function name in error message.  
	 */
	public void checkEmpty(String name) {
		if (size() == 0)
			return;
		String key = keySet().iterator().next();
		throw Utils.throwException("TypeError", name + " got an unexpected keyword argument '" + key + "'");
	}

	/** 
	 * Throws exception if there is any kwarg in list.  
	 */
	public void notExpectingKWArgs() {
		checkEmpty("function");
	}

}