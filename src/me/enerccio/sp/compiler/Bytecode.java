package me.enerccio.sp.compiler;

import me.enerccio.sp.compiler.PythonBytecode.*;
import me.enerccio.sp.types.base.CustomBytecode;

public enum Bytecode {
	// System
	NOP(0), 
	PUSH_ENVIRONMENT(8), POP_ENVIRONMENT(9), PUSH_DICT(10), PUSH_LOCAL_CONTEXT(11), 
	IMPORT(12), RESOLVE_ARGS(13), ACCEPT_RETURN(14), 
	
	// control
	RETURN(16), POP(17), PUSH(18), CALL(19), GOTO(20), JUMPIFTRUE(21), JUMPIFFALSE(22), DUP(23), SWAP_STACK(24),
	// variables
	LOAD(32), LOADGLOBAL(33), LOADNONLOCAL(34), SAVE(35), SAVEGLOBAL(36), SAVENONLOCAL(37), UNPACK_SEQUENCE(38),
	// exceptions
	PUSH_EXCEPTION_HANDLER(64), PUSH_FINALLY_HANDLER(65), POP_EXCEPTION_HANDLER(66), POP_FINALLY_HANDLER(67),
	END_EXCEPTION(68),
	
	// custom
	CUSTOM(255);
	
	Bytecode(int id){
		this.id = id;
	}
	
	public final int id;

	public static Bytecode fromNumber(int intValue) {
		for (Bytecode b : values())
			if (b.id == intValue)
				return b;
		return null;
	}

	public static PythonBytecode makeBytecode(Bytecode b) {
		PythonBytecode bytecode = null;
		
		switch (b) {
		case CALL:
			bytecode = new Call();
			bytecode.newObject();
			break;
		case CUSTOM:
			bytecode = new CustomBytecode();
			bytecode.newObject();
			break;
		case DUP:
			bytecode = new Dup();
			bytecode.newObject();
			break;
		case END_EXCEPTION:
			bytecode = new EndException();
			bytecode.newObject();
			break;
		case GOTO:
			bytecode = new Goto();
			bytecode.newObject();
			break;
		case JUMPIFFALSE:
			bytecode = new JumpIfFalse();
			bytecode.newObject();
			break;
		case JUMPIFTRUE:
			bytecode = new JumpIfTrue();
			bytecode.newObject();
			break;
		case LOAD:
			bytecode = new Load();
			bytecode.newObject();
			break;
		case LOADGLOBAL:
			bytecode = new LoadGlobal();
			bytecode.newObject();
			break;
		case LOADNONLOCAL:
			bytecode = new LoadNonLocal();
			bytecode.newObject();
			break;
		case NOP:
			bytecode = new Nop();
			bytecode.newObject();
			break;
		case POP:
			bytecode = new Pop();
			bytecode.newObject();
			break;
		case POP_ENVIRONMENT:
			bytecode = new PopEnvironment();
			bytecode.newObject();
			break;
		case POP_EXCEPTION_HANDLER:
			bytecode = new PopExceptionHandler();
			bytecode.newObject();
			break;
		case POP_FINALLY_HANDLER:
			bytecode = new PopFinallyHandler();
			bytecode.newObject();
			break;
		case PUSH:
			bytecode = new Push();
			bytecode.newObject();
			break;
		case PUSH_DICT:
			bytecode = new PushDict();
			bytecode.newObject();
			break;
		case PUSH_ENVIRONMENT:
			bytecode = new PushEnvironment();
			bytecode.newObject();
			break;
		case PUSH_EXCEPTION_HANDLER:
			bytecode = new PushExceptionHandler();
			bytecode.newObject();
			break;
		case PUSH_FINALLY_HANDLER:
			bytecode = new PushFinallyHandler();
			bytecode.newObject();
			break;
		case RETURN:
			bytecode = new Return();
			bytecode.newObject();
			break;
		case SAVE:
			bytecode = new Save();
			bytecode.newObject();
			break;
		case SAVEGLOBAL:
			bytecode = new SaveGlobal();
			bytecode.newObject();
			break;
		case SAVENONLOCAL:
			bytecode = new SaveNonLocal();
			bytecode.newObject();
			break;
		case IMPORT:
			bytecode = new Import();
			bytecode.newObject();
			break;
		case SWAP_STACK:
			bytecode = new SwapStack();
			bytecode.newObject();
			break;
		case UNPACK_SEQUENCE:
			bytecode = new UnpackSequence();
			bytecode.newObject();
			break;
		case PUSH_LOCAL_CONTEXT:
			bytecode = new PushLocalContext();
			bytecode.newObject();
			break;
		case RESOLVE_ARGS:
			bytecode = new ResolveArgs();
			bytecode.newObject();
			break;
		case ACCEPT_RETURN:
			bytecode = new AcceptReturn();
			bytecode.newObject();
			break;
		}
		
		return bytecode;
	}
}
