package me.enerccio.sp.interpret;

import java.util.Stack;

import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.CallableObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class PythonInterpret {

	public static final ThreadLocal<PythonInterpret> interpret = new ThreadLocal<PythonInterpret>();
	
	public PythonInterpret(){
		bind();
		currentGlobals.push(PythonRuntime.runtime.generateGlobals());
	}
	
	public void bind(){
		interpret.set(this);
	}
	
	private Stack<MapObject> currentGlobals = new Stack<MapObject>();
	private Stack<MapObject> currentLocals = new Stack<MapObject>();
	public Stack<PythonObject> currentContext = new Stack<PythonObject>();

	public PythonObject executeCall(String function, PythonObject... data) {
		MapObject locals = Utils.peek(currentLocals);
		MapObject globals = Utils.peek(currentGlobals);
		
		if (locals != null && locals.contains(function))
			return execute(execute(locals.doGet(function)), data);
		return execute(execute(globals.doGet(function)), data);
	}

	public PythonObject execute(PythonObject callable, PythonObject... args) {
		if (callable instanceof CallableObject){
			return ((CallableObject)callable).call(new TupleObject(args));
		} else {
			return execute(callable.get(CallableObject.__CALL__, null), args);
		}
	}

	public PythonObject invoke(PythonObject callable, TupleObject args) {
		return execute(callable, args.getObjects());
	}

	public PythonObject getLocalContext() {
		return Utils.peek(currentContext);
	}
	
}
