package me.enerccio.sp.interpret;

import java.util.List;
import java.util.Stack;

import me.enerccio.sp.parser.pythonParser.StmtContext;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.CallableObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class PythonInterpret {

	public static final ThreadLocal<PythonInterpret> interpret = new ThreadLocal<PythonInterpret>(){

		@Override
		protected PythonInterpret initialValue() {
			return new PythonInterpret();
		}
		
	};
	
	public PythonInterpret(){
		bind();
	}
	
	public void bind(){
		interpret.set(this);
	}
	
	public Stack<CurrentEnvironment> currentEnvironment = new Stack<CurrentEnvironment>();
	public Stack<PythonObject> currentContext = new Stack<PythonObject>();

	public PythonObject executeCall(String function, PythonObject... data) {
		return execute(environment().get(new StringObject(function), false), data);
	}

	public CurrentEnvironment environment() {
		return Utils.peek(currentEnvironment);
	}
	
	public PythonObject getLocalContext() {
		return Utils.peek(currentContext);
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

	public PythonObject getGlobal(String key) {
		return environment().get(new StringObject(key), true);
	}

	public void pushEnvironment(MapObject... environs) {
		CurrentEnvironment c;
		currentEnvironment.push(c = new CurrentEnvironment());
		c.add(environs);
	}

	public void runAst(List<StmtContext> statements) {
		for (StmtContext stmt : statements)
			executeStatement(stmt);
	}

	private void executeStatement(StmtContext stmt) {
		
	}
	
}
