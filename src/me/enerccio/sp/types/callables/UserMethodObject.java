package me.enerccio.sp.types.callables;

import java.util.ArrayList;
import java.util.List;

import me.enerccio.sp.compiler.Bytecode;
import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class UserMethodObject extends PythonObject {
	private static final long serialVersionUID = 6184279154550720464L;
	public static final String SELF = "__self__";
	public static final String FUNC = "__func__";
	public static final String ACCESSOR = "__access__";
	
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
	
	public List<PythonBytecode> methodCall(UserMethodObject o, TupleObject args) {
		PythonBytecode b = null;
		List<PythonBytecode> l = new ArrayList<PythonBytecode>();

		PythonObject callable = o.get(UserMethodObject.SELF, o);
		PythonObject caller = o.get(UserMethodObject.FUNC, o);
		
		// []
		l.add(Bytecode.makeBytecode(Bytecode.PUSH_ENVIRONMENT));
		// environments
		if (caller instanceof UserFunctionObject && caller.fields.containsKey("closure")){
			// add closure
			for (PythonObject d : ((TupleObject) caller.fields.get("closure").object).getObjects()){
				l.add(b = Bytecode.makeBytecode(Bytecode.PUSH_DICT));
				b.mapValue = (MapObject) d;	
			}
		} else {
			// add globals
			l.add(b = Bytecode.makeBytecode(Bytecode.PUSH_DICT));
			b.mapValue = new MapObject();
			l.add(b = Bytecode.makeBytecode(Bytecode.PUSH_DICT));
			b.mapValue = PythonRuntime.runtime.generateGlobals();
		}
		
		l.add(b = Bytecode.makeBytecode(Bytecode.PUSH));
		if ( fields.get(ACCESSOR) == null)
			b.value = NoneObject.NONE;
		else
			b.value = fields.get(ACCESSOR).object;
		// [ python object __accessor__ ]
		l.add(Bytecode.makeBytecode(Bytecode.PUSH_LOCAL_CONTEXT));
		// []
		
		l.add(b = Bytecode.makeBytecode(Bytecode.PUSH));
		b.value = caller;
		// [ callable ]
		l.add(b = Bytecode.makeBytecode(Bytecode.PUSH));
		b.value = callable;
		// [ callable, python object ]
		
		for (int i=0; i<args.len(); i++){
			l.add(b = Bytecode.makeBytecode(Bytecode.PUSH));
			b.value = args.valueAt(i);
			// [ callable, python object, python object*++ ]
		}
		// [ callable, python object, python object* ]
		l.add(b = Bytecode.makeBytecode(Bytecode.CALL));
		b.intValue = args.len() + 1;
		// []
		l.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN));
		// [ python object ]
		l.add(b = Bytecode.makeBytecode(Bytecode.RETURN));
		b.intValue = 1;
		// []
		return l;
	}
	
	public PythonObject call(TupleObject args) {
		PythonInterpret.interpret.get().executeBytecode(methodCall(this, args));
		return NoneObject.NONE; // returns immediately
	}

	@Override
	public PythonObject set(String key, PythonObject localContext,
			PythonObject value) {
		if (key.equals(SELF) || key.equals(FUNC) || key.equals(ACCESSOR))
			throw Utils.throwException("AttributeError", "'" + 
					Utils.run("str", Utils.run("type", this)) + "' object attribute '" + key + "' is read only");
		return super.set(key, localContext, value);
	}

	@Override
	protected String doToString() {
		return "<method " + fields.get(FUNC).object + " of object " + fields.get(SELF).object + ">"; // TODO
	}

	@Override
	public boolean truthValue() {
		return true;
	}
}
