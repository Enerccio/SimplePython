package me.enerccio.sp.types.types;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.mappings.MapObject;
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
	public PythonObject call(TupleObject args, MapObject kwargs) {
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
		
		ClassObject type = new ClassObject();
		Utils.putPublic(type, ClassObject.__NAME__, name);
		Utils.putPublic(type, ClassObject.__BASES__, bases);
		Utils.putPublic(type, ClassObject.__DICT__, dict);
		return type;
	}

	private PythonObject getTypeInformation(PythonObject pythonObject) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
