package me.enerccio.sp.interpret;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.compiler.PythonBytecode.*;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.callables.CallableObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class PythonInterpret extends PythonObject {
	private static final long serialVersionUID = -8039667108607710165L;
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
	public LinkedList<FrameObject> currentFrame = new LinkedList<FrameObject>();

	public PythonObject executeCall(String function, PythonObject... data) {
		return execute(environment().get(new StringObject(function), false, false), data);
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
			return execute(callable.get(CallableObject.__CALL__, getLocalContext()), args);
		}
	}

	public PythonObject invoke(PythonObject callable, TupleObject args) {
		return execute(callable, args.getObjects());
	}

	public PythonObject getGlobal(String key) {
		return environment().get(new StringObject(key), true, false);
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
		FrameObject o = currentFrame.getLast();
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
		
		Stack<PythonObject> stack = o.stack;
		switch (pythonBytecode.getOpcode()){
		case NOP:
			break;
		case POP_ENVIRONMENT:
			currentEnvironment.pop();
			break;
		case PUSH_DICT:{
				EnvironmentObject env = Utils.peek(currentEnvironment);
				env.add(pythonBytecode.dict);
			} break;
		case PUSH_ENVIRONMENT:
			currentEnvironment.push(new EnvironmentObject());
			break;
		case CALL:
			PythonObject[] args = new PythonObject[pythonBytecode.argc];
			for (int i=0; i<args.length; i++)
				args[i] = stack.pop();
			
			break;
		case GOTO:
			o.pc = pythonBytecode.idx;
			break;
		case JUMPIFFALSE:
			if (!stack.pop().truthValue())
				o.pc = pythonBytecode.idx;
			break;
		case JUMPIFTRUE:
			if (stack.pop().truthValue())
				o.pc = pythonBytecode.idx;
			break;
		case LOAD:
			stack.push(environment().get(new StringObject(pythonBytecode.variable), false, false));
			break;
		case LOADGLOBAL:
			stack.push(environment().get(new StringObject(pythonBytecode.variable), true, false));
			break;
		case LOADNONLOCAL:
			stack.push(environment().get(new StringObject(pythonBytecode.variable), false, true));
			break;
		case POP:
			stack.pop();
			break;
		case PUSH:
			stack.push(pythonBytecode.value);
			break;
		case RETURN:
			currentEnvironment.pop();
			PythonObject retVal = pythonBytecode.value;
			if (retVal == null)
				retVal = NoneObject.NONE;
			currentFrame.pop();
			if (currentFrame.size() != 0)
				currentFrame.getLast().stack.add(retVal);
			break;
		case SAVE:
			environment().set(new StringObject(((Save)pythonBytecode).variable), stack.pop(), false, false);
			break;
		case SAVEGLOBAL:
			environment().set(new StringObject(((Save)pythonBytecode).variable), stack.pop(), true, false);
			break;
		case SAVENONLOCAL:
			environment().set(new StringObject(((Save)pythonBytecode).variable), stack.pop(), false, true);
			break;
		case CUSTOM:
			execute(pythonBytecode, environment(), this, PythonRuntime.runtime.runtimeWrapper());
			break;
		case POP_EXCEPTION_HANDLER:
			// TODO
			break;
		case POP_FINALLY_HANDLER:
			// TODO
			break;
		case PUSH_EXCEPTION_HANDLER:
			// TODO
			break;
		case PUSH_FINALLY_HANDLER:
			// TODO
			break;
		case DUP:
			stack.push(stack.peek());
			break;
		case END_EXCEPTION:
			// TODO
			break;
		}
		
	}

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	protected String doToString() {
		return "<bound python interpret for thread 0x" + Long.toHexString(Thread.currentThread().getId()) + ">";
	}
}
