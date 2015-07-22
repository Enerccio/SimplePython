package me.enerccio.sp.types.types;

import java.util.ArrayList;

import me.enerccio.sp.compiler.Bytecode;
import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.types.base.ClassInstanceObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.callables.UserFunctionObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class ObjectTypeObject extends ClassObject {
	private static final long serialVersionUID = 4583318830595686027L;
	public static final String OBJECT_CALL = "object";
	public static final String __CONTAINS__ = "__contains__";
	public static final String IS = "is";
	
	public static final ObjectTypeObject inst = new ObjectTypeObject();
	
	@Override
	public void newObject() {
		super.newObject();
		Utils.putPublic(this, "__name__", new StringObject("object"));
		Utils.putPublic(this, "__bases__", new TupleObject());
		MapObject md = null;
		Utils.putPublic(this, "__dict__", md = new MapObject());
		
		UserFunctionObject usf = new UserFunctionObject();
		usf.newObject();
		Utils.putPublic(usf, "__name__", new StringObject("object.__init__"));
		usf.args = new ArrayList<String>();
		usf.args.add("self");
		Utils.putPublic(usf, "function_defaults", new MapObject());
		PythonBytecode cb;
		usf.bytecode.add(Bytecode.makeBytecode(Bytecode.PUSH_ENVIRONMENT));
		usf.bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH));
		cb.value = NoneObject.NONE;
		usf.bytecode.add(Bytecode.makeBytecode(Bytecode.RETURN));
		
		md.put(ClassInstanceObject.__INIT__, usf);
	}
	
	@Override
	protected String doToString() {
		return "<type object>";
	}

}
