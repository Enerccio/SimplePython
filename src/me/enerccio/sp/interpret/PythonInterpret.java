package me.enerccio.sp.interpret;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.compiler.PythonBytecode.PushDict;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.CallableObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class PythonInterpret {

	public static final transient ThreadLocal<PythonInterpret> interpret = new ThreadLocal<PythonInterpret>(){

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
	
	public Stack<EnvironmentObject> currentEnvironment = new Stack<EnvironmentObject>();
	public Stack<PythonObject> currentContext = new Stack<PythonObject>();
	public Stack<FrameObject> currentFrame = new Stack<FrameObject>();

	public PythonObject executeCall(String function, PythonObject... data) {
		return execute(environment().get(new StringObject(function), false), data);
	}

	public EnvironmentObject environment() {
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
		EnvironmentObject c;
		currentEnvironment.push(c = new EnvironmentObject());
		c.add(environs);
	}

	public void executeBytecode(List<PythonBytecode> frame) {
		FrameObject o;
		currentFrame.add(o = new FrameObject());
		o.bytecode = new ArrayList<PythonBytecode>(frame);
	}

	public ExecutionResult executeOnce(){
		try {
			PythonRuntime.runtime.waitIfSaving();
		} catch (InterruptedException e) {
			return ExecutionResult.INTERRUPTED;
		}
		
		if (Thread.interrupted()){
			return ExecutionResult.INTERRUPTED;
		}
		FrameObject o = Utils.peek(currentFrame);
		if (o == null)
			return ExecutionResult.FINISHED;
		if (o.pc >= o.bytecode.size()){
			currentFrame.pop();
			return executeOnce();
		}
		executeSingleInstruction(o, o.bytecode.get(o.pc++));
		return ExecutionResult.OK;
	}

	private void executeSingleInstruction(FrameObject o,
			PythonBytecode pythonBytecode) {
		
		switch (pythonBytecode.getOpcode()){
		case NOP:
			break;
		case POP_ENVIRONMENT:
			currentEnvironment.pop();
			break;
		case PUSH_DICT:{
				EnvironmentObject env = Utils.peek(currentEnvironment);
				env.add(((PushDict)pythonBytecode).dict);
			} break;
		case PUSH_ENVIRONMENT:
			currentEnvironment.push(new EnvironmentObject());
			break;
		
		}
		
	}
}
