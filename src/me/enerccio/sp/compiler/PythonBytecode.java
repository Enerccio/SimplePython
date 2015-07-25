package me.enerccio.sp.compiler;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.mappings.MapObject;

public class PythonBytecode extends PythonObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -34522216612624880L;
	public int debugLine = -1;
	public int debugInLine;
	public String debugModule = "";

	public int intValue;
	public PythonObject value;
	public String stringValue;
	public String stringValue2;
	public MapObject mapValue;
	public boolean booleanValue;
	
	public PythonBytecode(){
		
	}
	
	protected Bytecode bytecode;
	public Bytecode getOpcode(){
		return bytecode;
	}
	
	public static class Nop extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 438692838919066014L;

		{
			bytecode = Bytecode.NOP;
		}
	}
	
	public static class SaveLocal extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 438692838919066015L;

		{
			bytecode = Bytecode.SAVE_LOCAL;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), stringValue);
		}
	}
	
	public static class Raise extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 438692838919066015L;

		{
			bytecode = Bytecode.SAVE_LOCAL;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), stringValue);
		}
	}

	public static class Reraise extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -6312861151510715111L;

		{
			bytecode = Bytecode.RERAISE;
		}
	}
	
	public static class AcceptReturn extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6332909908617239072L;

		{
			bytecode = Bytecode.ACCEPT_RETURN;
		}
	}
	
	public static class PushEnvironment extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3569826021524513052L;

		{
			bytecode = Bytecode.PUSH_ENVIRONMENT;
		}
	}
	
	public static class PushLocalContext extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1088681858920031098L;

		{
			bytecode = Bytecode.PUSH_LOCAL_CONTEXT;
		}
	}
	
	public static class PopEnvironment extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5706402557880603973L;

		{
			bytecode = Bytecode.POP_ENVIRONMENT;
		}
	}
	
	public static class PushDict extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2091671127142060613L;

		{
			bytecode = Bytecode.PUSH_DICT;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), mapValue);
		}
	}
	
	public static class Return extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7601997081632281638L;

		{
			bytecode = Bytecode.RETURN;
		}

		@Override
		protected String doToString() {
			if (intValue == 1)
				return String.format("%s (%s - returns value)", getOpcode().toString(), intValue);
			return String.format("%s (%s - exits frame)", getOpcode().toString(), intValue);
		}

	}
	
	public static class Pop extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -211343001041855798L;

		{
			bytecode = Bytecode.POP;
		}
	}
	
	public static class Push extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2322622705042387021L;

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
		/**
		 * 
		 */
		private static final long serialVersionUID = -3080128534823197157L;

		{
			bytecode = Bytecode.CALL;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), intValue);
		}
	}

	public static class RCall extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 9058117934717120328L;

		{
			bytecode = Bytecode.RCALL;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), intValue);
		}
	}
	
	public static class Goto extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1236413271543232839L;

		{
			bytecode = Bytecode.GOTO;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), intValue);
		}
	}
	
	public static class Label extends PythonBytecode {
		private static final long serialVersionUID = 3323548781923254906L;

		{
			bytecode = Bytecode.LABEL;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), stringValue);
		}
	}
	
	public static class JumpIfTrue extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 941692152211238078L;

		{
			bytecode = Bytecode.JUMPIFTRUE;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), intValue);
		}
	}
	
	public static class JumpIfNone extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2507334203455848750L;

		{
			bytecode = Bytecode.JUMPIFNONE;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), intValue);
		}
	}

	public static class IsInstance extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2053918345821186800L;

		{
			bytecode = Bytecode.ISINSTANCE;
		}
	}
	
	public static class JumpIfNoReturn extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5203909925032492085L;

		{
			bytecode = Bytecode.JUMPIFNORETURN;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), intValue);
		}
	}

	public static class JumpIfFalse extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2622975371669812361L;

		{
			bytecode = Bytecode.JUMPIFFALSE;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), intValue);
		}
	}
	
	public static class Load extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1903853394223900780L;

		{
			bytecode = Bytecode.LOAD;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), stringValue);
		}

	}

	public static class LoadGlobal extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1567806659588614058L;

		{
			bytecode = Bytecode.LOADGLOBAL;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), stringValue);
		}
	}
	
	public static class Save extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -406468849835914035L;

		{
			bytecode = Bytecode.SAVE;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), stringValue);
		}
	}
	
	public static class SaveGlobal extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1109972611003866956L;

		{
			bytecode = Bytecode.SAVEGLOBAL;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), stringValue);
		}
	}
	
	public static class Dup extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8214786407927555266L;

		{
			bytecode = Bytecode.DUP;
		}
	}
	
	public static class PushFrame extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8927402167434919757L;

		{
			bytecode = Bytecode.PUSH_FRAME;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), intValue);
		}
	}

	public static class PushException extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8296967861579722494L;

		{
			bytecode = Bytecode.PUSH_EXCEPTION;
		}
	}
	
	public static class Import extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2882854084616515036L;

		{
			bytecode = Bytecode.IMPORT;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s, %s)", getOpcode().toString(), stringValue2, stringValue);
		}
		
	}
	
	public static class SwapStack extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7862981632281325840L;

		{
			bytecode = Bytecode.SWAP_STACK;
		}
	}

	public static class UnpackSequence extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 9049013845798886791L;

		{
			bytecode = Bytecode.UNPACK_SEQUENCE;
		}

		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), intValue);
		}
	}
	
	public static class ResolveArgs extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1200159557331063544L;

		{
			bytecode = Bytecode.RESOLVE_ARGS;
		}
	}
	
	public static class GetAttr extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -9210115051088548884L;

		{
			bytecode = Bytecode.GETATTR;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), stringValue);
		}
	}
	
	public static class SetAttr extends PythonBytecode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -127124418723016892L;

		{
			bytecode = Bytecode.SETATTR;
		}
		
		@Override
		protected String doToString() {
			return String.format("%s(%s)", getOpcode().toString(), stringValue);
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
