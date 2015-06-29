package me.enerccio.sp.types.types;

import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;

public class IntTypeObject extends TypeObject {
	private static final long serialVersionUID = -4178003762513900453L;
	public static final String INT_CALL = "int";

	public IntTypeObject(){
		
	}

	@Override
	public String getTypeIdentificator() {
		return "int";
	}

	@Override
	public PythonObject call(TupleObject args, MapObject kwargs) {
		PythonObject arg1 = null, arg2 = null;
		
		if (args.size().intValue() == 0){
			arg1 = new IntObject(0);
		} else if (args.size().intValue() == 1){
			arg1 = args.getObjects()[0];
		} else if (args.size().intValue() == 2){
			arg1 = args.getObjects()[0];
			arg2 = args.getObjects()[1];
		}
		
		if (arg2 == null)
			return PythonInterpret.interpret.get().execute(arg1.get(IntObject.__INT__, null));
		else {
			try {
				PythonObject strArg = PythonInterpret.interpret.get().executeCall(StringTypeObject.STRING_CALL, arg2);
				PythonObject intArg = PythonInterpret.interpret.get().executeCall(INT_CALL, arg2);
				return new IntObject(Integer.parseInt(((StringObject)strArg).getString(), ((IntObject)intArg).intValue()));
			} catch (ClassCastException e){
				// TODO
			}
		}
		
		// TODO error
		throw new RuntimeException();
	}

}
