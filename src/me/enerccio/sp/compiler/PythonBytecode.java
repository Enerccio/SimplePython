package me.enerccio.sp.compiler;

import java.util.List;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.mappings.MapObject;

@SuppressWarnings("serial")
public class PythonBytecode extends PythonObject {

	public PythonObject value;
	public String variable;
	public MapObject dict;
	public PythonObject callable;
	public int idx;
	public int argc;
	public PythonObject exceptionType;
	public int jumpAfter;
	public List<PythonBytecode> ast;
	public String moduleName;
	
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
	
	public static class PushEnvironment extends PythonBytecode {
		{
			bytecode = Bytecode.PUSH_ENVIRONMENT;
		}
	}
	
	public static class PushLocalContext extends PythonBytecode {
		{
			bytecode = Bytecode.PUSH_LOCAL_CONTEXT;
		}
	}
	
	public static class PopEnvironment extends PythonBytecode {
		{
			bytecode = Bytecode.POP_ENVIRONMENT;
		}
	}
	
	public static class PushDict extends PythonBytecode {
		{
			bytecode = Bytecode.PUSH_DICT;
		}
	}
	
	public static class Return extends PythonBytecode {
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
		{
			bytecode = Bytecode.PUSH;
		}
	}

	public static class Call extends PythonBytecode {
		{
			bytecode = Bytecode.CALL;
		}
	}
	
	public static class Goto extends PythonBytecode {
		{
			bytecode = Bytecode.GOTO;
		}
	}
	
	public static class JumpIfTrue extends PythonBytecode {
		{
			bytecode = Bytecode.JUMPIFTRUE;
		}
	}
	
	public static class JumpIfFalse extends PythonBytecode {
		{
			bytecode = Bytecode.JUMPIFFALSE;
		}
	}
	
	public static class Load extends PythonBytecode {
		{
			bytecode = Bytecode.LOAD;
		}
	}

	public static class LoadGlobal extends PythonBytecode {
		{
			bytecode = Bytecode.LOADGLOBAL;
		}
	}

	public static class LoadNonLocal extends PythonBytecode {
		{
			bytecode = Bytecode.LOADNONLOCAL;
		}
	}

	public static class Save extends PythonBytecode {
		{
			bytecode = Bytecode.SAVE;
		}
	}
	
	public static class SaveGlobal extends PythonBytecode {
		{
			bytecode = Bytecode.SAVEGLOBAL;
		}
	}
	
	public static class SaveNonLocal extends PythonBytecode {
		{
			bytecode = Bytecode.SAVENONLOCAL;
		}
	}
	
	public static class PushExceptionHandler extends PythonBytecode {
		{
			bytecode = Bytecode.PUSH_EXCEPTION_HANDLER;
		}
	}
	
	public static class PushFinallyHandler extends PythonBytecode {
		{
			bytecode = Bytecode.PUSH_FINALLY_HANDLER;
		}
	}
	
	public static class PopExceptionHandler extends PythonBytecode {
		{
			bytecode = Bytecode.POP_EXCEPTION_HANDLER;
		}
	}
	
	public static class PopFinallyHandler extends PythonBytecode {
		{
			bytecode = Bytecode.POP_FINALLY_HANDLER;
		}
	}
	
	public static class Dup extends PythonBytecode {
		{
			bytecode = Bytecode.DUP;
		}
	}
	
	public static class EndException extends PythonBytecode {
		{
			bytecode = Bytecode.END_EXCEPTION;
		}
	}
	
	public static class Import extends PythonBytecode {
		{
			bytecode = Bytecode.IMPORT;
		}
	}
	
	public static class SwapStack extends PythonBytecode {
		{
			bytecode = Bytecode.SWAP_STACK;
		}
	}

	public static class UnpackSequence extends PythonBytecode {
		{
			bytecode = Bytecode.UNPACK_SEQUENCE;
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
