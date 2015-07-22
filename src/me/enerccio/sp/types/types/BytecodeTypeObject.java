package me.enerccio.sp.types.types;

import me.enerccio.sp.compiler.Bytecode;
import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.compiler.PythonBytecode.AcceptReturn;
import me.enerccio.sp.compiler.PythonBytecode.Call;
import me.enerccio.sp.compiler.PythonBytecode.Dup;
import me.enerccio.sp.compiler.PythonBytecode.Goto;
import me.enerccio.sp.compiler.PythonBytecode.Import;
import me.enerccio.sp.compiler.PythonBytecode.JumpIfFalse;
import me.enerccio.sp.compiler.PythonBytecode.JumpIfTrue;
import me.enerccio.sp.compiler.PythonBytecode.Load;
import me.enerccio.sp.compiler.PythonBytecode.LoadGlobal;
import me.enerccio.sp.compiler.PythonBytecode.Nop;
import me.enerccio.sp.compiler.PythonBytecode.Pop;
import me.enerccio.sp.compiler.PythonBytecode.PopEnvironment;
import me.enerccio.sp.compiler.PythonBytecode.Push;
import me.enerccio.sp.compiler.PythonBytecode.PushDict;
import me.enerccio.sp.compiler.PythonBytecode.PushEnvironment;
import me.enerccio.sp.compiler.PythonBytecode.PushLocalContext;
import me.enerccio.sp.compiler.PythonBytecode.ResolveArgs;
import me.enerccio.sp.compiler.PythonBytecode.Return;
import me.enerccio.sp.compiler.PythonBytecode.Save;
import me.enerccio.sp.compiler.PythonBytecode.SaveGlobal;
import me.enerccio.sp.compiler.PythonBytecode.SwapStack;
import me.enerccio.sp.compiler.PythonBytecode.UnpackSequence;
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
			throw Utils.throwException("TypeError", "bytecode(): incorrect number of parameters, must be >0");
		
		try {
			IntObject byteNum = (IntObject) args.getObjects()[0];
			
			Bytecode b = Bytecode.fromNumber(byteNum.intValue());
			if (b == null)
				throw Utils.throwException("TypeError", "unknown bytecode number");
			PythonBytecode bytecode = null;
			
			switch (b) {
			case CALL:
				bytecode = new Call();
				bytecode.newObject();
				bytecode.value = args.getObjects()[1];
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
			case GOTO:
				bytecode = new Goto();
				bytecode.newObject();
				bytecode.intValue = ((IntObject) args.getObjects()[1]).intValue();
				break;
			case JUMPIFFALSE:
				bytecode = new JumpIfFalse();
				bytecode.newObject();
				bytecode.intValue = ((IntObject) args.getObjects()[1]).intValue();
				break;
			case JUMPIFTRUE:
				bytecode = new JumpIfTrue();
				bytecode.newObject();
				bytecode.intValue = ((IntObject) args.getObjects()[1]).intValue();
				break;
			case LOAD:
				bytecode = new Load();
				bytecode.newObject();
				bytecode.stringValue = ((StringObject) args.getObjects()[1]).value;
				break;
			case LOADGLOBAL:
				bytecode = new LoadGlobal();
				bytecode.newObject();
				bytecode.stringValue = ((StringObject) args.getObjects()[1]).value;
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
				bytecode.value = args.getObjects()[1];
				break;
			case PUSH_DICT:
				bytecode = new PushDict();
				bytecode.newObject();
				bytecode.mapValue = (MapObject) args.getObjects()[1];
				break;
			case PUSH_ENVIRONMENT:
				bytecode = new PushEnvironment();
				bytecode.newObject();
				break;
//				bytecode.ast = new ArrayList<PythonBytecode>(Utils.asList(args.getObjects()[1]));
//				for (PythonBytecode pb : bytecode.ast)
//					; // just check that it is all inded python bytecode not some other crap data
//				break;
			case RETURN:
				bytecode = new Return();
				bytecode.newObject();
				bytecode.value = args.getObjects()[1];
				break;
			case SAVE:
				bytecode = new Save();
				bytecode.newObject();
				bytecode.stringValue = ((StringObject) args.getObjects()[1]).value;
				break;
			case SAVEGLOBAL:
				bytecode = new SaveGlobal();
				bytecode.newObject();
				bytecode.stringValue = ((StringObject) args.getObjects()[1]).value;
				break;
			case IMPORT:
				bytecode = new Import();
				bytecode.stringValue2 = ((StringObject) args.getObjects()[1]).value;
				bytecode.stringValue = ((StringObject) args.getObjects()[2]).value;
				bytecode.newObject();
				break;
			case SWAP_STACK:
				bytecode = new SwapStack();
				bytecode.newObject();
				break;
			case UNPACK_SEQUENCE:
				bytecode = new UnpackSequence();
				bytecode.newObject();
				bytecode.intValue = ((IntObject) args.getObjects()[1]).intValue();
				break;
			case PUSH_LOCAL_CONTEXT:
				bytecode = new PushLocalContext();
				bytecode.newObject();
				bytecode.value = args.getObjects()[1];
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
		} catch (ClassCastException e){
			throw Utils.throwException("TypeError", "bytecode(): incorrect type of arguments");
		} catch (ArrayIndexOutOfBoundsException e){
			throw Utils.throwException("TypeError", "bytecode(): incorrect number of arguments");
		}
		
	}
}
