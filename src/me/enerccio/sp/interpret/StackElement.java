package me.enerccio.sp.interpret;

import me.enerccio.sp.runtime.ModuleInfo;
import me.enerccio.sp.types.PythonObject;

/** Container for stack data */
public class StackElement extends PythonObject {
	private static final long serialVersionUID = -3288411350030175582L;
	public static final StackElement SYSTEM_FRAME = new StackElement(); 
	public static final StackElement LAST_FRAME = new StackElement();
	
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
}