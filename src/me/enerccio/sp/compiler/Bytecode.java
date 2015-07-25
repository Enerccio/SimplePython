package me.enerccio.sp.compiler;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.Token;

import me.enerccio.sp.compiler.PythonBytecode.*;
import me.enerccio.sp.types.base.CustomBytecode;
import me.enerccio.sp.types.callables.UserFunctionObject;

public enum Bytecode {
	// System
	NOP(0), 
	PUSH_ENVIRONMENT(8), POP_ENVIRONMENT(9), PUSH_DICT(10), PUSH_LOCAL_CONTEXT(11), 
	IMPORT(12), RESOLVE_ARGS(13), ACCEPT_RETURN(14), PUSH_FRAME(15), PUSH_EXCEPTION(16),
	
	// control
	POP(17), PUSH(18), CALL(19), RCALL(20), DUP(21), SWAP_STACK(22),
	JUMPIFTRUE(23), JUMPIFFALSE(24), JUMPIFNONE(25), JUMPIFNORETURN(26),
	GOTO(27), RETURN(28), LABEL(31), 
	// variables
	LOAD(32), LOADGLOBAL(33), SAVE(35), SAVEGLOBAL(36), UNPACK_SEQUENCE(38),
	// exceptions
	RAISE(69), RERAISE(70),
	// macros
	GETATTR(90), SETATTR(90), ISINSTANCE(91), 
	// frames 
	
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
		return makeBytecode(b, null);
	}

	public static PythonBytecode makeBytecode(Bytecode b, Token t) {
		PythonBytecode bytecode = null;
		
		switch (b) {
		case CALL:
			bytecode = new Call();
			bytecode.newObject();
			break;
		case RCALL:
			bytecode = new RCall();
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
		case GOTO:
			bytecode = new Goto();
			bytecode.newObject();
			break;
		case LABEL:
			bytecode = new Label();
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
		case JUMPIFNONE:
			bytecode = new JumpIfNone();
			bytecode.newObject();
			break;
		case JUMPIFNORETURN:
			bytecode = new JumpIfNoReturn();
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
		case GETATTR:
			bytecode = new GetAttr();
			bytecode.newObject();
			break;
		case SETATTR:
			bytecode = new SetAttr();
			bytecode.newObject();
			break;
		case ISINSTANCE:
			bytecode = new IsInstance();
			bytecode.newObject();
			break;
		case RAISE:
			bytecode = new Raise();
			bytecode.newObject();
			break;
		case RERAISE:
			bytecode = new Reraise();
			bytecode.newObject();
			break;
		case PUSH_FRAME:
			bytecode = new PushFrame();
			bytecode.newObject();
			break;
		case PUSH_EXCEPTION:
			bytecode = new PushException();
			bytecode.newObject();
			break;
		}
		
		bytecode.debugModule = PythonCompiler.moduleName.get();
		if (t != null){
			bytecode.debugLine = t.getLine();
			bytecode.debugInLine = t.getCharPositionInLine();
		}
		
		return bytecode;
	}
	
	public static String dis(List<PythonBytecode> bcl) {
		return dis(bcl, 0);
	}
	
	public static String dis(int i, PythonBytecode bc) {
		String s = bc.toString();
		int cut = s.length();
		if (cut > 80) cut  = 80;
		return String.format("%05d \t%S" , i, s.substring(0, cut));
	}

	
	public static String dis(List<PythonBytecode> bcl, int offset) {
		Map<String, List<PythonBytecode>> built = new LinkedHashMap<String, List<PythonBytecode>>();
		StringBuilder b = new StringBuilder();
		for (int i=offset; i<bcl.size(); i++) {
			PythonBytecode bc = bcl.get(i);
			if (bc.value != null && bc.value instanceof UserFunctionObject)
				built.put(bc.value.toString(), ((UserFunctionObject)bc.value).bytecode);
			b.append(dis(i, bc));
			b.append("\n");
		}
		
		for (String key : built.keySet()){
			b.append("\n");
			b.append(key + "\n");
			
			b.append(dis(built.get(key), 0));
		}
		
		return b.toString();
	}
}
