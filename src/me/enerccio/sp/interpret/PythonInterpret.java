package me.enerccio.sp.interpret;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.compiler.PythonBytecode.*;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.ModuleObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.callables.CallableObject;
import me.enerccio.sp.types.callables.UserFunctionObject;
import me.enerccio.sp.types.callables.UserMethodObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.mappings.PythonProxy;
import me.enerccio.sp.types.sequences.OrderedSequenceIterator;
import me.enerccio.sp.types.sequences.SequenceObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class PythonInterpret extends PythonObject {
	private static final long serialVersionUID = -8039667108607710165L;
	public static final transient ThreadLocal<PythonInterpret> interpret = new ThreadLocal<PythonInterpret>(){

		@Override
		protected PythonInterpret initialValue() {
			try {
				PythonRuntime.runtime.waitForNewInterpretAvailability();
			} catch (InterruptedException e){
				
			}
			
			PythonInterpret i = new PythonInterpret();
			i.newObject();
			interprets.add(i);
			return i;
		}
		
	};
	
	public static final Set<PythonInterpret> interprets = Collections.synchronizedSet(new HashSet<PythonInterpret>());
	
	public PythonInterpret(){
		bind();
	}
	
	public void bind(){
		interpret.set(this);
	}
	
	public Stack<EnvironmentObject> currentEnvironment = new Stack<EnvironmentObject>();
	public Stack<PythonObject> currentContext = new Stack<PythonObject>();
	public LinkedList<FrameObject> currentFrame = new LinkedList<FrameObject>();
	private volatile int accessCount = 0;
	private MapObject args = null;
	private PythonObject returnee;
	
	public boolean isInterpretStoppable(){
		return accessCount == 0;
	}

	public PythonObject executeCall(String function, PythonObject... data) {
		return execute(false, environment().get(new StringObject(function), false, false), data);
	}

	public EnvironmentObject environment() {
		return Utils.peek(currentEnvironment);
	}
	
	public PythonObject getLocalContext() {
		return Utils.peek(currentContext);
	}

	public PythonObject execute(boolean internalCall, PythonObject callable, PythonObject... args) {
		if (callable instanceof CallableObject){
			if (((callable instanceof UserFunctionObject) || (callable instanceof UserMethodObject)) && internalCall){
				int cfc = currentFrame.size();
				((CallableObject)callable).call(new TupleObject(args));
				while (true){
					ExecutionResult res = PythonInterpret.interpret.get().executeOnce();
					if (res == ExecutionResult.FINISHED || res == ExecutionResult.EOF)
						if (PythonInterpret.interpret.get().currentFrame.size() == cfc)
							return returnee;
				}
			} else
				return ((CallableObject)callable).call(new TupleObject(args));
		} else {
			return execute(false, callable.get(CallableObject.__CALL__, getLocalContext()), args);
		}
	}

	public PythonObject invoke(PythonObject callable, TupleObject args) {
		return execute(false, callable, args.getObjects());
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
		o.recalculateLabels();
	}

	public ExecutionResult executeOnce(){
		try {
			PythonRuntime.runtime.waitIfSaving(this);
		} catch (InterruptedException e) {
			return ExecutionResult.INTERRUPTED;
		}
		
		if (Thread.interrupted()){
			return ExecutionResult.INTERRUPTED;
		}
		
		++accessCount;
		try {
			if (currentFrame.size() == 0)
				return ExecutionResult.FINISHED;
			FrameObject o = currentFrame.getLast();
			if (o == null)
				return ExecutionResult.FINISHED;
			if (o.pc >= o.bytecode.size()){
				currentFrame.removeLast();
				returnee = null;
				return ExecutionResult.EOF;
			}
			return executeSingleInstruction(o, o.bytecode.get(o.pc++));
		} finally {
			--accessCount;
		}
	}

	private ExecutionResult executeSingleInstruction(FrameObject o,
			PythonBytecode pythonBytecode) {
		
		Stack<PythonObject> stack = o.stack;
		switch (pythonBytecode.getOpcode()){
		case NOP:
			break;
		case LABEL:
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
		case CALL: {
			int argl = pythonBytecode.argc >= 0 ? pythonBytecode.argc : -pythonBytecode.argc;
			boolean va = false;
			if (pythonBytecode.argc < 0)
				va = true;
			PythonObject[] args = new PythonObject[argl];
			
			for (int i=args.length-1; i>=0; i--)
				args[i] = stack.pop();
			PythonObject runnable = stack.pop();
			
			if (va){
				PythonObject[] va2 = args;
				PythonObject tp = va2[va2.length-1];
				if (!(tp instanceof TupleObject))
					throw Utils.throwException("TypeError", "Last argument must be a 'tuple'");
				PythonObject[] vra = ((TupleObject)tp).getObjects();
				args = new PythonObject[va2.length - 1 + vra.length];
				for (int i=0; i<va2.length; i++)
					args[i] = va2[i];
				for (int i=0; i<vra.length; i++)
					args[i + va2.length] = va2[i];
			}
			
			returnee = execute(true, runnable, args);
			break;
		}
		case RCALL: {
			PythonObject[] args = new PythonObject[pythonBytecode.argc];
			
			for (int i=0; i<args.length; i++)
				args[i] = stack.pop();
			PythonObject runnable = stack.pop();
			
			returnee = execute(true, runnable, args);
			break;
		}
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
			PythonObject value = environment().get(new StringObject(pythonBytecode.variable), false, false);
			if (value == null)
				throw Utils.throwException("NameError", "name " + pythonBytecode.variable + " is not defined");
			stack.push(value);
			break;
		case LOADGLOBAL:
			value = environment().get(new StringObject(pythonBytecode.variable), true, false);
			if (value == null)
				throw Utils.throwException("NameError", "name " + pythonBytecode.variable + " is not defined");
			stack.push(value);
			break;
		case POP:
			stack.pop();
			break;
		case PUSH:
			stack.push(pythonBytecode.value);
			break;
		case RETURN:
			currentEnvironment.pop();
			PythonObject retVal = stack.pop();
			currentFrame.removeLast();
			returnee = retVal;
			return ExecutionResult.EOF;
		case SAVE:
			environment().set(new StringObject(((Save)pythonBytecode).variable), stack.pop(), false, false);
			break;
		case SAVEGLOBAL:
			environment().set(new StringObject(((Save)pythonBytecode).variable), stack.pop(), true, false);
			break;
		case CUSTOM:
			execute(true, pythonBytecode, environment(), this, PythonRuntime.runtime.runtimeWrapper());
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
		case IMPORT:
			ModuleObject mm = (ModuleObject) 
				environment().get(new StringObject(ModuleObject.__THISMODULE__), true, false);
			String resolvePath = mm.provider.getPackageResolve() != null ? mm.provider.getPackageResolve() : "";
			resolvePath += resolvePath.equals("") ? pythonBytecode.moduleName : "." + pythonBytecode.moduleName;
			pythonImport(environment(), pythonBytecode.variable, resolvePath, null);
			break;
		case SWAP_STACK: {
			PythonObject top = stack.pop();
			PythonObject bot = stack.pop();
			stack.push(top);
			stack.push(bot);
			break;
		}
		case UNPACK_SEQUENCE:
			PythonObject seq = stack.pop();
			PythonObject iterator = execute(true, Utils.get(seq, SequenceObject.__ITER__));
			PythonObject[] ss = new PythonObject[pythonBytecode.argc];
			PythonObject stype = environment().get(new StringObject("StopIteration"), true, false);
			
			try {
				for (int i=ss.length-1; i>=0; i--){
					ss[i] = execute(true, Utils.get(iterator, OrderedSequenceIterator.NEXT));
				}
			} catch (PythonExecutionException e){
				if (Utils.run("isinstance", e.getException(), stype).truthValue()){
					throw Utils.throwException("ValueError", "Too few values to unpack");
				} else
					throw e;
			}
			
			try {
				execute(true, Utils.get(iterator, OrderedSequenceIterator.NEXT));
				throw Utils.throwException("ValueError", "Too many values to unpack");
			} catch (PythonExecutionException e){
				if (!Utils.run("isinstance", e.getException(), stype).truthValue()){
					throw e;
				}
			}
			
			for (PythonObject obj : ss)
				stack.push(obj);
			
			break;
		case PUSH_LOCAL_CONTEXT:
			currentContext.add(stack.pop());
			break;
		case RESOLVE_ARGS:
			synchronized (this.args.backingMap){
				for (PythonProxy key : this.args.backingMap.keySet()){
					environment().set((StringObject) key.o, this.args.backingMap.get(key), false, false);
				}
			}
			break;
		case ACCEPT_RETURN:
			if (returnee == null)
				stack.push(NoneObject.NONE);
			else
				stack.push(returnee);
			break;
		case GETATTR: {
			PythonObject runnable = environment().get(new StringObject("getattr"), true, false);
			PythonObject[] args = new PythonObject[2];
			// If argument for GETATTR is not set, attribute name is pop()ed from stack   
			if (pythonBytecode.variable == null) {
				args[1] = stack.pop();	// attribute
				args[0] = stack.pop();	// object
			} else {
				args[0] = stack.pop();									// object
				args[1] = new StringObject(pythonBytecode.variable);	// attribute
			} 
			returnee = execute(true, runnable, args);
			stack.push(returnee);
			break;
		}
		case SETATTR: {
			PythonObject runnable = environment().get(new StringObject("setattr"), true, false);
			PythonObject[] args = new PythonObject[3];
			// If argument for SETATTR is not set, attribute name is pop()ed from stack   
			if (pythonBytecode.value == null) {
				args[1] = stack.pop();	// attribute
				args[0] = stack.pop();	// object
				args[2] = stack.pop();	// value
			} else {
				args[1] = new StringObject(pythonBytecode.variable);	// attribute
				args[0] = stack.pop();									// object
				args[2] = stack.pop();									// value
			} 
			returnee = execute(true, runnable, args);
			stack.push(returnee);
			break;
		}
		}
		return ExecutionResult.OK;
	}

	private void pythonImport(EnvironmentObject environment, String variable,
			String modulePath, PythonObject target) {
		if (modulePath == null || modulePath.equals("")){
			if (target == null){
				synchronized (PythonRuntime.runtime){
					target = PythonRuntime.runtime.root.get(variable);
					if (target == null)
						target = PythonRuntime.runtime.getModule(variable, null);
				}
			} else if (!variable.equals("*")){
				environment.set(new StringObject(variable), 
						target,
						false, false);
			} else {
				MapObject dict = (MapObject) target.fields.get(ModuleObject.__DICT__).object;
				synchronized (dict){
					synchronized (dict.backingMap){
						for (PythonProxy key : dict.backingMap.keySet()){
							if (key.o instanceof StringObject){
								String kkey = ((StringObject)key.o).value;
								if (!kkey.startsWith("__"))
									environment.set((StringObject)key.o, 
											dict.backingMap.get(key),
											false, false);
							}
						}
					}
				}
				
			}
		} else {
			String[] split = modulePath.split("\\.");
			String mm = split[0];
			modulePath = modulePath.replaceFirst(mm, "");
			modulePath = modulePath.replaceFirst("\\.", "");
			if (target == null){
				target = (ModuleObject) environment.get(new StringObject(mm), false, false);
				if (target == null)
					synchronized (PythonRuntime.runtime){
						target = PythonRuntime.runtime.root.get(mm);
						if (target == null)
							target = PythonRuntime.runtime.getModule(mm, null);
					}
			} else {
				if (target instanceof ModuleObject){
					ModuleObject mod = (ModuleObject)target;
					PythonObject target2 = ((MapObject)mod.fields.get(ModuleObject.__DICT__).object).doGet(mm);
					if (target2 != null){
						pythonImport(environment, variable, modulePath, target2);
						return;
					}
				} 
				if (target.fields.containsKey(mm)) {
					pythonImport(environment, variable, modulePath, target.fields.get(mm).object);
					return;
				} else {
					target = PythonRuntime.runtime.getModule(mm, new StringObject(((ModuleObject)target).provider.getPackageResolve()));
				}
			}
			pythonImport(environment, variable, modulePath, target);
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

	public void setArgs(MapObject a) {
		args = a;
	}

	public FrameObject frame() {
		return currentFrame.peekLast();
	}
}
