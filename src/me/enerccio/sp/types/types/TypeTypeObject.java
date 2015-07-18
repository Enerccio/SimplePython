package me.enerccio.sp.types.types;

import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.ClassInstanceObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.base.SliceObject;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.sequences.ListObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class TypeTypeObject extends TypeObject {
	private static final long serialVersionUID = -9154234544871833082L;
	public static final String TYPE_CALL = "type";
	
	@Override
	public String getTypeIdentificator() {
		return "type";
	}

	@Override
	public PythonObject call(TupleObject args) {
		if (args.size().intValue() == 1)
			return getTypeInformation(args.getObjects()[0]);
		else if (args.size().intValue() == 3)
			return newClassType(args.getObjects()[0], args.getObjects()[1], args.getObjects()[2]);
		
		throw Utils.throwException("TypeError", "Incorrect number of parameters");
	}

	private PythonObject newClassType(PythonObject name,
			PythonObject bases, PythonObject dict) {
		if (!(name instanceof StringObject))
			throw Utils.throwException("TypeError", "Name must be a string");
		if (!(bases instanceof TupleObject))
			throw Utils.throwException("TypeError", "Bases must be a tuple");
		if (!(dict instanceof MapObject))
			throw Utils.throwException("TypeError", "Dict must be a dict");
		
		PythonObject object = PythonInterpret.interpret.get().getGlobal("object");
		TupleObject bb = (TupleObject)bases;
		if (bb.size().intValue() == 0){
			bases = new TupleObject(object);
		} else if (bb.getObjects()[0] != object){
			bases = new TupleObject(Utils.pushLeft(object, bb.getObjects()));
		}
		
		ClassObject type = new ClassObject();
		Utils.putPublic(type, ClassObject.__NAME__, name);
		Utils.putPublic(type, ClassObject.__BASES__, bases);
		Utils.putPublic(type, ClassObject.__DICT__, dict);
		return type;
	}

	private PythonObject getTypeInformation(PythonObject py) {
		if (py instanceof PythonBytecode)
			return new BytecodeTypeObject();
		if (py instanceof IntObject)
			return new IntTypeObject();
		if (py instanceof ListObject)
			return new ListTypeObject();
		if (py instanceof ClassInstanceObject)
			return ((ClassInstanceObject)py).get(ClassObject.__CLASS__, py);
		if (py instanceof ClassObject)
			return new TypeTypeObject();
		if (py instanceof SliceObject)
			return new SliceTypeObject();
		if (py instanceof TupleObject)
			return new TupleTypeObject();
		if (py instanceof StringObject)
			return new StringTypeObject();
		return null;
	}
	
}
