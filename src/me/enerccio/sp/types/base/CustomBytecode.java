package me.enerccio.sp.types.base;

import me.enerccio.sp.compiler.Bytecode;
import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.CallableObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class CustomBytecode extends PythonBytecode {
	private static final long serialVersionUID = 388228361390637623L;
	public static final String OPERATION = "__op__";
	
	@Override
	public void newObject() {
		super.newObject();
		
		try {
			Utils.putPublic(this, CallableObject.__CALL__, new JavaMethodObject(this, this.getClass().getMethod("call", 
					new Class<?>[]{TupleObject.class}), true));
			Utils.putPublic(this, OPERATION, NoneObject.NONE);
		} catch (NoSuchMethodException e){
			// will not happen
		}
	};
	
	@Override
	public Bytecode getOpcode() {
		return Bytecode.CUSTOM;
	}

	public PythonObject call(TupleObject args){
		return PythonInterpret.interpret.get().execute(get(OPERATION, this), args.getObjects());
	}
}
