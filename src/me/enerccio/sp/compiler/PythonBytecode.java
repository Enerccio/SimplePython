package me.enerccio.sp.compiler;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.mappings.MapObject;

@SuppressWarnings("serial")
public class PythonBytecode extends PythonObject {
	public PythonBytecode(){
		
	}
	
	protected Bytecode bytecode;
	public Bytecode getOpcode(){
		return bytecode;
	}

	public static class Nop extends PythonBytecode {
		{
			bytecode = Bytecode.NOP;
		}
	}
	
	public static class PushEnvirnment extends PythonBytecode {
		{
			bytecode = Bytecode.PUSH_ENVIRONMENT;
		}
	}
	
	public static class PopEnvironment extends PythonBytecode {
		{
			bytecode = Bytecode.POP_ENVIRONMENT;
		}
	}
	
	public static class PushDict extends PythonBytecode {
		public MapObject dict;
		{
			bytecode = Bytecode.PUSH_DICT;
		}
	}

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	protected String doToString() {
		return "<bytecode " + getOpcode().toString() + ">";
	}
	
}
