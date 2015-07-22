package me.enerccio.sp.compiler;

import java.util.List;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.mappings.MapObject;

@SuppressWarnings("serial")
public class PythonBytecode extends PythonObject {
	
	public int debugLine = -1;
	public int debugInLine;
	public String debugModule = "";

	public PythonObject value;
	public String variable;
	public MapObject dict;
	public PythonObject callable;
	public int argc;
	public PythonObject exceptionType;
	public int jumpAfter;
	public List<PythonBytecode> ast;
	public String moduleName;
	public boolean stackException = false;
	
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
	
	public static class Raise extends PythonBytecode {
		{
			bytecode = Bytecode.RAISE;
		}
	}

	public static class Reraise extends PythonBytecode {
		{
			bytecode = Bytecode.RERAISE;
		}
	}
	
	public static class Label extends PythonBytecode {
		{
			bytecode = Bytecode.LABEL;
		}
	}
	
	public static class AcceptReturn extends PythonBytecode {
		{
			bytecode = Bytecode.ACCEPT_RETURN;
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
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), dict);
		}
	}
	
	public static class Return extends PythonBytecode {
		{
			bytecode = Bytecode.RETURN;
		}

		@Override
		protected String doToString() {
			if (argc == 1)
				return String.format("%s (%s - returns value)", getOpcode().toString(), argc);
			return String.format("%s (%s - exits frame)", getOpcode().toString(), argc);
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
		
		@Override
		protected String doToString() {
			try {
				return String.format("%s(%s)", getOpcode().toString(), value.toString());
			} catch (Exception e) {
				return String.format("%s(%s)", getOpcode().toString(), e);
			}
		}
	}

	public static class Call extends PythonBytecode {
		{
			bytecode = Bytecode.CALL;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), argc);
		}
	}

	public static class RCall extends PythonBytecode {
		{
			bytecode = Bytecode.RCALL;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), argc);
		}
	}
	
	public static class Goto extends PythonBytecode {
		{
			bytecode = Bytecode.GOTO;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), argc);
		}
	}
	
	public static class JumpIfTrue extends PythonBytecode {
		{
			bytecode = Bytecode.JUMPIFTRUE;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), argc);
		}
	}

	public static class IsInstance extends PythonBytecode {
		{
			bytecode = Bytecode.ISINSTANCE;
		}
	}
	
	public static class JumpIfNoReturn extends PythonBytecode {
		{
			bytecode = Bytecode.JUMPIFNORETURN;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), argc);
		}
	}

	public static class JumpIfFalse extends PythonBytecode {
		{
			bytecode = Bytecode.JUMPIFFALSE;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), argc);
		}
	}
	
	public static class Load extends PythonBytecode {
		{
			bytecode = Bytecode.LOAD;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), variable);
		}

	}

	public static class LoadGlobal extends PythonBytecode {
		{
			bytecode = Bytecode.LOADGLOBAL;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), variable);
		}
	}
	
	public static class Save extends PythonBytecode {
		{
			bytecode = Bytecode.SAVE;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), variable);
		}
	}
	
	public static class SaveGlobal extends PythonBytecode {
		{
			bytecode = Bytecode.SAVEGLOBAL;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), variable);
		}
	}
	
	public static class Dup extends PythonBytecode {
		{
			bytecode = Bytecode.DUP;
		}
	}
	
	public static class PushFrame extends PythonBytecode {
		{
			bytecode = Bytecode.PUSH_FRAME;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), argc);
		}
	}

	public static class PushException extends PythonBytecode {
		{
			bytecode = Bytecode.PUSH_EXCEPTION;
		}
	}
	
	public static class Import extends PythonBytecode {
		{
			bytecode = Bytecode.IMPORT;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s, %s)", getOpcode().toString(), moduleName, variable);
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

		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), argc);
		}
	}
	
	public static class ResolveArgs extends PythonBytecode {
		{
			bytecode = Bytecode.RESOLVE_ARGS;
		}
	}
	
	public static class GetAttr extends PythonBytecode {
		{
			bytecode = Bytecode.GETATTR;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), variable);
		}
	}
	
	public static class SetAttr extends PythonBytecode {
		{
			bytecode = Bytecode.SETATTR;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), variable);
		}
	}

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	protected String doToString() {
		return getOpcode().toString();
	}
	
}
