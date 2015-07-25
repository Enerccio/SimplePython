package me.enerccio.sp.types.types;

import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.BoolObject;
import me.enerccio.sp.types.base.ClassInstanceObject;
import me.enerccio.sp.types.base.ComplexObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.base.SliceObject;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.callables.JavaFunctionObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.callables.UserFunctionObject;
import me.enerccio.sp.types.callables.UserMethodObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.pointer.PointerObject;
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
		if (args.len() == 1)
			return getTypeInformation(args.getObjects()[0]);
		else if (args.len() == 3)
			return newClassType(args.getObjects()[0], args.getObjects()[1], args.getObjects()[2]);
		
		throw Utils.throwException("TypeError", " type(): incorrect number of parameters");
	}

	private PythonObject newClassType(PythonObject name,
			PythonObject bases, PythonObject dict) {
		if (!(name instanceof StringObject))
			throw Utils.throwException("TypeError", "type(): name must be a string");
		if (!(bases instanceof TupleObject))
			throw Utils.throwException("TypeError", "type(): bases must be a tuple");
		if (!(dict instanceof MapObject))
			throw Utils.throwException("TypeError", "type(): dict must be a dict");

		ClassObject type = new ClassObject();
		type.newObject();
		Utils.putPublic(type, ClassObject.__NAME__, name);
		Utils.putPublic(type, ClassObject.__BASES__, bases);
		Utils.putPublic(type, ClassObject.__DICT__, dict);
		return type;
	}

	private PythonObject getTypeInformation(PythonObject py) {
		if (py instanceof PythonBytecode)
			return Utils.getGlobal(BytecodeTypeObject.BYTECODE_CALL);
		if (py instanceof IntObject)
			return Utils.getGlobal(IntTypeObject.INT_CALL);
		if (py instanceof ListObject)
			return Utils.getGlobal(ListTypeObject.LIST_CALL);
		if (py instanceof ClassInstanceObject)
			return ((ClassInstanceObject)py).get(ClassObject.__CLASS__, py);
		if (py instanceof ClassObject)
			return Utils.getGlobal(TYPE_CALL);
		if (py instanceof SliceObject)
			return Utils.getGlobal(SliceTypeObject.SLICE_CALL);
		if (py instanceof TupleObject)
			return Utils.getGlobal(TupleTypeObject.TUPLE_CALL);
		if (py instanceof MapObject)
			return Utils.getGlobal(DictTypeObject.DICT_CALL);
		if (py instanceof StringObject)
			return Utils.getGlobal(StringTypeObject.STRING_CALL);
		if (py instanceof PointerObject)
			return Utils.getGlobal(JavaInstanceTypeObject.JAVA_CALL);
		if (py instanceof UserFunctionObject)
			return Utils.getGlobal(FunctionTypeObject.FUNCTION_CALL);
		if (py instanceof UserMethodObject)
			return Utils.getGlobal(MethodTypeObject.METHOD_CALL);
		if (py instanceof BoolObject)
			return Utils.getGlobal(BoolTypeObject.BOOL_CALL);
		if (py instanceof JavaMethodObject || py instanceof JavaFunctionObject)
			return Utils.getGlobal(JavaCallableTypeObject.JAVACALLABLE_CALL);
		if (py instanceof ComplexObject)
			return Utils.getGlobal(ComplexTypeObject.COMPLEX_CALL);

		return NoneObject.NONE;
	}
	
}
