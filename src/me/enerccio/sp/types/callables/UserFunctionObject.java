package me.enerccio.sp.types.callables;

import java.util.ArrayList;
import java.util.List;

import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.mappings.MapObject;
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
		int argc = args.len();
		int rargs = this.args.size();
		if (isVararg)
			++rargs;
		
		if (argc < rargs)
			throw Utils.throwException("TypeError", "Incorrect amount of arguments, expected at least " + rargs + ", got " + args.len());
		
		if (!isVararg && argc > rargs)
			throw Utils.throwException("TypeError", "Incorrect amount of arguments, expected at most " + rargs + ", got " + args.len());
			
		
		MapObject a = new MapObject();
		a.newObject();
		
		List<PythonObject> vargs = new ArrayList<PythonObject>();
		for (int i=0; i<argc; i++){
			if (i < this.args.size())
				a.put(this.args.get(i), args.getObjects()[i]);
			else
				vargs.add(args.getObjects()[i]);
		}
		
		if (isVararg){
			a.put(vararg, Utils.list2tuple(vargs));
		}
		
		PythonInterpret.interpret.get().setArgs(a);
		PythonInterpret.interpret.get().executeBytecode(bytecode);
		
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
