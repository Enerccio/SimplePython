package me.enerccio.sp.compiler;

import java.util.List;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.mappings.MapObject;

@SuppressWarnings("serial")
public class PythonBytecode extends PythonObject {
	
	public int debugLine;
	public int debugInLine;
	public String debugModule = "";

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
		
		/*
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), dict);
		}*/
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
