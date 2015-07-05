package me.enerccio.sp.interpret;

import java.util.List;

import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.types.PythonObject;

public class FrameObject extends PythonObject {
	private static final long serialVersionUID = 3202634156179178037L;
	
	public List<PythonBytecode> bytecode;
	public int pc;

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	protected String doToString() {
		return "<frame object 0x" + Integer.toHexString(hashCode()) + ">";
	}

}
