package me.enerccio.sp.types.callables;

import java.util.ArrayList;
import java.util.List;

import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class UserFunctionObject extends PythonObject {
	private static final long serialVersionUID = 22L;
	
	public List<PythonBytecode> bytecode = new ArrayList<PythonBytecode>();
	public List<String> args;
	public boolean isVararg;
	public String vararg;
	
	public UserFunctionObject(){
		
	}
	
	@Override
	public void newObject() {
		super.newObject();
		
		try {
			Utils.putPublic(this, CallableObject.__CALL__, new JavaMethodObject(this, this.getClass().getMethod("call", 
					new Class<?>[]{TupleObject.class}), true));
		} catch (NoSuchMethodException e){
			// will not happen
		}
	};

	public PythonObject call(TupleObject args) {
		// TODO
		
		return NoneObject.NONE; // returns immediately
	}

	@Override
	protected String doToString() {
		return "<function " + fields.get("__name__").object + ">"; // TODO
	}

	@Override
	public boolean truthValue() {
		return true;
	}
}
