package me.enerccio.sp.compiler;

public enum Bytecode {
	// System
	NOP(0), 
	PUSH_ENVIRONMENT(8), POP_ENVIRONMENT(9), PUSH_DICT(10), 
	
	// control
	RETURN(16), POP(17), PUSH(18), CALL(19), GOTO(20), JUMPIFTRUE(21), JUMPIFFALSE(22), DUP(23),
	YIELD(24), 
	// variables
	LOAD(32), LOADGLOBAL(33), LOADNONLOCAL(34), SAVE(35), SAVEGLOBAL(36), SAVENONLOCAL(37),
	// exceptions
	PUSH_EXCEPTION_HANDLER(64), PUSH_FINALLY_HANDLER(65), POP_EXCEPTION_HANDLER(66), POP_FINALLY_HANDLER(67),
	END_EXCEPTION(68),
	
	// custom
	CUSTOM(255);
	
	Bytecode(int id){
		this.id = id;
	}
	
	public final int id;
}
