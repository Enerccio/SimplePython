package me.enerccio.sp.types.types;

import java.util.ArrayList;

import me.enerccio.sp.compiler.Bytecode;
import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.compiler.PythonBytecode.*;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.CustomBytecode;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class BytecodeTypeObject extends TypeObject {
	private static final long serialVersionUID = 1434651099156262641L;
	public static final String BYTECODE_CALL = "bytecode";

	@Override
	public String getTypeIdentificator() {
		return "bytecode";
	}

	@SuppressWarnings("unused")
	@Override
	public PythonObject call(TupleObject args) {
		if (args.size().intValue() == 0)
			throw Utils.throwException("TypeError", "Incorrect number of parameters, must be >0");
		
		try {
			IntObject byteNum = (IntObject) args.getObjects()[0];
			
			Bytecode b = Bytecode.fromNumber(byteNum.intValue());
			if (b == null)
				throw Utils.throwException("TypeError", "Unknown bytecode number");
			PythonBytecode bytecode = null;
			
			switch (b) {
			case CALL:
				bytecode = new Call();
				bytecode.newObject();
				bytecode.callable = args.getObjects()[1];
				break;
			case CUSTOM:
				bytecode = new CustomBytecode();
				bytecode.newObject();
				Utils.putPublic(bytecode, CustomBytecode.OPERATION, args.getObjects()[1]);
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
				bytecode.idx = ((IntObject) args.getObjects()[1]).intValue();
				break;
			case JUMPIFFALSE:
				bytecode = new JumpIfFalse();
				bytecode.newObject();
				bytecode.idx = ((IntObject) args.getObjects()[1]).intValue();
				break;
			case JUMPIFTRUE:
				bytecode = new JumpIfTrue();
				bytecode.newObject();
				bytecode.idx = ((IntObject) args.getObjects()[1]).intValue();
				break;
			case LOAD:
				bytecode = new Load();
				bytecode.newObject();
				bytecode.variable = ((StringObject) args.getObjects()[1]).value;
				break;
			case LOADGLOBAL:
				bytecode = new LoadGlobal();
				bytecode.newObject();
				bytecode.variable = ((StringObject) args.getObjects()[1]).value;
				break;
			case LOADNONLOCAL:
				bytecode = new LoadNonLocal();
				bytecode.newObject();
				bytecode.variable = ((StringObject) args.getObjects()[1]).value;
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
				bytecode.exceptionType = args.getObjects()[1];
				break;
			case POP_FINALLY_HANDLER:
				bytecode = new PopFinallyHandler();
				bytecode.newObject();
				bytecode.ast = new ArrayList<PythonBytecode>(Utils.asList(args.getObjects()[1]));
				for (PythonBytecode pb : bytecode.ast)
					; // just check that it is all inded python bytecode not some other crap data
				break;
			case PUSH:
				bytecode = new Push();
				bytecode.newObject();
				bytecode.value = args.getObjects()[1];
				break;
			case PUSH_DICT:
				bytecode = new PushDict();
				bytecode.newObject();
				bytecode.dict = (MapObject) args.getObjects()[1];
				break;
			case PUSH_ENVIRONMENT:
				bytecode = new PushEnvironment();
				bytecode.newObject();
				break;
			case PUSH_EXCEPTION_HANDLER:
				bytecode = new PushExceptionHandler();
				bytecode.newObject();
				bytecode.exceptionType = args.getObjects()[1];
				break;
			case PUSH_FINALLY_HANDLER:
				bytecode = new PushFinallyHandler();
				bytecode.newObject();
				bytecode.ast = new ArrayList<PythonBytecode>(Utils.asList(args.getObjects()[1]));
				for (PythonBytecode pb : bytecode.ast)
					; // just check that it is all inded python bytecode not some other crap data
				break;
			case RETURN:
				bytecode = new Return();
				bytecode.newObject();
				bytecode.value = args.getObjects()[1];
				break;
			case SAVE:
				bytecode = new Save();
				bytecode.newObject();
				bytecode.variable = ((StringObject) args.getObjects()[1]).value;
				break;
			case SAVEGLOBAL:
				bytecode = new SaveGlobal();
				bytecode.newObject();
				bytecode.variable = ((StringObject) args.getObjects()[1]).value;
				break;
			case SAVENONLOCAL:
				bytecode = new SaveNonLocal();
				bytecode.newObject();
				bytecode.variable = ((StringObject) args.getObjects()[1]).value;
				break;
			case IMPORT:
				bytecode = new Import();
				bytecode.moduleName = ((StringObject) args.getObjects()[1]).value;
				bytecode.variable = ((StringObject) args.getObjects()[2]).value;
				bytecode.newObject();
				break;
			case SWAP_STACK:
				bytecode = new SwapStack();
				bytecode.newObject();
				break;
			}
			
			return bytecode;
		} catch (ClassCastException e){
			throw Utils.throwException("TypeError", "Incorrect type of arguments");
		} catch (ArrayIndexOutOfBoundsException e){
			throw Utils.throwException("TypeError", "Incorrect number of arguments");
		}
		
	}
}
