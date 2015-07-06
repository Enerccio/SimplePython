package me.enerccio.sp.compiler;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.NoneObject;
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
	
	public static class Return extends PythonBytecode {
		public PythonObject value = NoneObject.NONE;
		{
			bytecode = Bytecode.RETURN;
		}
	}
	
	public static class Pop extends PythonBytecode {
		{
			bytecode = Bytecode.POP;
		}
	}
	
	public static class Push extends PythonBytecode {
		public PythonObject value;
		{
			bytecode = Bytecode.PUSH;
		}
	}

	public static class Call extends PythonBytecode {
		public PythonObject callable;
		int argc;
		{
			bytecode = Bytecode.CALL;
		}
	}
	
	public static class Goto extends PythonBytecode {
		public int idx;
		{
			bytecode = Bytecode.GOTO;
		}
	}
	
	public static class JumpIfTrue extends PythonBytecode {
		public int idx;
		{
			bytecode = Bytecode.JUMPIFTRUE;
		}
	}
	
	public static class JumpIfFalse extends PythonBytecode {
		public int idx;
		{
			bytecode = Bytecode.JUMPIFFALSE;
		}
	}
	
	public static class Load extends PythonBytecode {
		public String variable;
		{
			bytecode = Bytecode.LOAD;
		}
	}

	public static class LoadGlobal extends PythonBytecode {
		public String variable;
		{
			bytecode = Bytecode.LOADGLOBAL;
		}
	}
	

	public static class LoadNonLocal extends PythonBytecode {
		public String variable;
		{
			bytecode = Bytecode.LOADNONLOCAL;
		}
	}

	public static class Save extends PythonBytecode {
		public String variable;
		{
			bytecode = Bytecode.SAVE;
		}
	}
	
	public static class SaveGlobal extends PythonBytecode {
		public String variable;
		{
			bytecode = Bytecode.SAVEGLOBAL;
		}
	}
	
	public static class SaveNonLocal extends PythonBytecode {
		public String variable;
		{
			bytecode = Bytecode.SAVENONLOCAL;
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
