package me.enerccio.sp.interpret;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import me.enerccio.sp.compiler.Bytecode;
import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.compiler.PythonBytecode.*;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.ModuleObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.BoolObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.callables.CallableObject;
import me.enerccio.sp.types.callables.UserFunctionObject;
import me.enerccio.sp.types.callables.UserMethodObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.mappings.PythonProxy;
import me.enerccio.sp.types.sequences.ListObject;
import me.enerccio.sp.types.sequences.OrderedSequenceIterator;
import me.enerccio.sp.types.sequences.SequenceObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

@SuppressWarnings("unused")
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
		if (currentEnvironment.size() == 0)
			return returnee = execute(false, PythonRuntime.runtime.generateGlobals().doGet(function), data);
		return returnee = execute(false, environment().get(new StringObject(function), false, false), data);
	}

	public EnvironmentObject environment() {
		return Utils.peek(currentEnvironment);
	}
	
	public PythonObject getLocalContext() {
		PythonObject p = Utils.peek(currentContext);
		if (p == null)
			return NoneObject.NONE;
		return p;
	}

	public PythonObject execute(boolean internalCall, PythonObject callable, PythonObject... args) {
		if (callable instanceof CallableObject){
			if (((callable instanceof UserFunctionObject) || (callable instanceof UserMethodObject)) && internalCall){
				int cfc = currentFrame.size();
				((CallableObject)callable).call(new TupleObject(args));
				while (true){
					ExecutionResult res = PythonInterpret.interpret.get().executeOnce();
					if (res == ExecutionResult.FINISHED || res == ExecutionResult.EOF)
						if (PythonInterpret.interpret.get().currentFrame.size() == cfc){
							if (PythonInterpret.interpret.get().exception() != null){
								PythonObject e = PythonInterpret.interpret.get().exception();
								PythonInterpret.interpret.get().currentFrame.peekLast().exception = null;
								throw new PythonExecutionException(e);
							}
							return returnee;
						}
				}
			} else
				return returnee = ((CallableObject)callable).call(new TupleObject(args));
		} else {
			PythonObject callableArg = callable.get(CallableObject.__CALL__, getLocalContext());
			if (callableArg == null)
				throw Utils.throwException("TypeError", callable.toString() + " is not callable");
			return returnee = execute(false, callableArg, args);
		}
	}

	public PythonObject exception() {
		if (currentFrame.size() != 0)
			return currentFrame.peekLast().exception;
		return null;
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
		FrameObject n;
		currentFrame.add(n = new FrameObject());
		n.bytecode = new ArrayList<PythonBytecode>(frame);
	}

	public ExecutionResult executeOnce(){
		ExecutionResult r = doExecuteOnce();
		if (r == ExecutionResult.EOF)
			if (currentFrame.size() == 0)
				return ExecutionResult.FINISHED;
		return r;
	}

	private ExecutionResult doExecuteOnce() {
		try {
			PythonRuntime.runtime.waitIfSaving(this);
		} catch (InterruptedException e) {
			return ExecutionResult.INTERRUPTED;
		}
		
		if (Thread.interrupted()){
			return ExecutionResult.INTERRUPTED;
		}
		
		if (exception() != null){
			handleException(new PythonExecutionException(exception()));
			return ExecutionResult.EOF;
		}
		
		++accessCount;
		try {
			if (currentFrame.size() == 0)
				return ExecutionResult.FINISHED;
			FrameObject o = currentFrame.getLast();
			if (o == null)
				return ExecutionResult.FINISHED;
			if (o.pc >= o.bytecode.size()){
				removeLastFrame();
				returnee = null;
				return ExecutionResult.EOF;
			}
			try {
				return executeSingleInstruction(o, o.bytecode.get(o.pc++));
			} catch (PythonExecutionException e){
				handleException(e);
				return ExecutionResult.EOF;
			}
		} finally {
			--accessCount;
		}
	}

	private void handleException(PythonExecutionException e) {
		currentFrame.peekLast().exception = e.getException();
		PythonObject stack = Utils.run("getattr", exception(), new StringObject("stack"));
		if (stack instanceof ListObject){
			ListObject s = (ListObject)stack;
			s.objects.add(makeStack());
		}
		removeLastFrame();
	}

	private StringObject makeStack() {
		return new StringObject(makeStackString());
	}

	private String makeStackString() {
		FrameObject o = currentFrame.getLast();
		if (o == null)
			return "<last frame>";
		if (o.debugLine < 0)
			return "<method-call>";
		return String.format("<at module %s, line %s, char %s>", o.debugModule, o.debugLine, o.debugInLine);
	}

	private ExecutionResult executeSingleInstruction(FrameObject o,
			PythonBytecode pythonBytecode) {
		
		o.debugModule = pythonBytecode.debugModule;
		o.debugLine = pythonBytecode.debugLine;
		o.debugInLine = pythonBytecode.debugInLine;
		
		Stack<PythonObject> stack = o.stack;
//		if (pythonBytecode.getOpcode() != Bytecode.ACCEPT_RETURN)
//			System.out.println("<" + o.debugModule + ", " + o.debugLine + "> \t\t" + o + " \t\t" + Bytecode.dis(o.pc - 1, pythonBytecode));
//		else
//			System.out.println("<" + o.debugModule + ", " + o.debugLine + "> \t\t" + o + " \t\t" + Bytecode.dis(o.pc - 1, pythonBytecode) + " value: " + returnee);
		switch (pythonBytecode.getOpcode()){
		case NOP:
			break;
		case PUSH_DICT:{
				EnvironmentObject env = Utils.peek(currentEnvironment);
				env.add(pythonBytecode.mapValue);
			} break;
		case PUSH_ENVIRONMENT:
			currentEnvironment.push(new EnvironmentObject());
			currentFrame.getLast().pushed_environ = true;
			break;
		case CALL: {
			int argl = pythonBytecode.intValue >= 0 ? pythonBytecode.intValue : -pythonBytecode.intValue;
			boolean va = false;
			if (pythonBytecode.intValue < 0)
				va = true;
			PythonObject[] args = new PythonObject[argl];
			
			for (int i=args.length-1; i>=0; i--)
				args[i] = stack.pop();
			PythonObject runnable = stack.pop();
			
			if (va){
				PythonObject[] va2 = args;
				PythonObject tp = va2[va2.length-1];
				if (!(tp instanceof TupleObject))
					throw Utils.throwException("TypeError", returnee + ": last argument must be a 'tuple'");
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
		case ECALL:
			// Leaves called method on top of stack
			// Pushes frame in which call was called
			FrameObject frame;
			PythonObject runnable = stack.peek();
			try {
				returnee = execute(true, runnable);
				frame = currentFrame.peekLast();
				if (frame != o)
					frame.parentFrame = o;
				else
					stack.push(o);
			} catch (PythonExecutionException e) {
				frame = new FrameObject();
				frame.parentFrame = o;
				frame.exception = e.getException();
				stack.push(frame);
			}
			break;
		case ACCEPT_ITER:
			// Replaces frame left by ECALL with returnee value.
			// If StopIteration was raised, jumps to specified bytecode
			// Any other exception is rised again
			frame = (FrameObject) stack.pop();
			if (frame.exception != null) {
				PythonObject stype = environment().get(new StringObject("StopIteration"), true, false);
				if (Utils.run("isinstance", frame.exception, stype).truthValue()) {
					o.pc = pythonBytecode.intValue;
					o.exception = frame.exception = null;
					break;
				}
				throw new PythonExecutionException(frame.exception);
			}
			if (returnee == null)
				stack.push(NoneObject.NONE);
			else
				stack.push(returnee);
			break;
		case RCALL: {
			PythonObject[] args = new PythonObject[pythonBytecode.intValue];
			
			for (int i=0; i<args.length; i++)
				args[i] = stack.pop();
			runnable = stack.pop();
			
			returnee = execute(true, runnable, args);
			break;
		}
		case GOTO:
			o.pc = pythonBytecode.intValue;
			break;
		case JUMPIFFALSE:
			if (!stack.pop().truthValue())
				o.pc = pythonBytecode.intValue;
			break;
		case JUMPIFTRUE:
			if (stack.pop().truthValue())
				o.pc = pythonBytecode.intValue;
			break;
		case JUMPIFNONE:
			// Peeks, leaves value on stack
			if (stack.peek() == NoneObject.NONE)
				o.pc = pythonBytecode.intValue;
			break;
		case JUMPIFNORETURN:
			frame = (FrameObject) stack.peek();
			if (!frame.returnHappened)
				// Frame ended without return, jump to specified label and keep frame on stack
				o.pc = pythonBytecode.intValue;
			break;
		case LOAD: 
			PythonObject value = environment().get(new StringObject(pythonBytecode.stringValue), false, false);
			if (value == null)
				throw Utils.throwException("NameError", "name " + pythonBytecode.stringValue + " is not defined");
			stack.push(value);
			break;
		case LOADGLOBAL:
			value = environment().get(new StringObject(pythonBytecode.stringValue), true, false);
			if (value == null)
				throw Utils.throwException("NameError", "name " + pythonBytecode.stringValue + " is not defined");
			stack.push(value);
			break;
		case POP:
			stack.pop();
			break;
		case PUSH:
			stack.push(pythonBytecode.value);
			break;
		case RETURN:
			if (pythonBytecode.intValue == 1) {
				o.returnHappened = true;
				PythonObject retVal = stack.pop();
				returnee = retVal;
			}
			removeLastFrame();
			return ExecutionResult.EOF;
		case SAVE:
			environment().set(new StringObject(((Save)pythonBytecode).stringValue), stack.pop(), false, false);
			break;
		case SAVE_LOCAL:
			environment().getLocals().backingMap.put(new StringObject(((SaveLocal)pythonBytecode).stringValue), stack.pop());
			break;
		case SAVEGLOBAL:
			environment().set(new StringObject(((Save)pythonBytecode).stringValue), stack.pop(), true, false);
			break;
		case CUSTOM:
			execute(true, pythonBytecode, environment(), this, PythonRuntime.runtime.runtimeWrapper());
			break;
		case DUP:
			if (pythonBytecode.intValue == 0)
				stack.push(stack.peek());
			else
				stack.push(stack.get(stack.size() - 1 - pythonBytecode.intValue));
			break;
		case IMPORT:
			ModuleObject mm = (ModuleObject) 
				environment().get(new StringObject(ModuleObject.__THISMODULE__), true, false);
			String resolvePath = mm != null ? (mm.provider.getPackageResolve() != null ? mm.provider.getPackageResolve() : "") : "";
			resolvePath += resolvePath.equals("") ? pythonBytecode.stringValue2 : "." + pythonBytecode.stringValue2;
			pythonImport(environment(), pythonBytecode.stringValue, resolvePath, null);
			break;
		case SWAP_STACK: {
			PythonObject top = stack.pop();
			PythonObject bot = stack.pop();
			stack.push(top);
			stack.push(bot);
			break;
		}
		case UNPACK_SEQUENCE:
			int cfc = currentFrame.size();
			PythonObject seq = stack.pop();
			PythonObject iterator;
			PythonObject[] ss = new PythonObject[pythonBytecode.intValue];
			PythonObject stype = environment().get(new StringObject("StopIteration"), true, false);
			
			try {
				Utils.run("iter", seq);
				if (currentFrame.size() != cfc)
					executeAll(cfc);
				iterator = returnee;
				
				for (int i=ss.length-1; i>=0; i--){
					returnee = execute(true, Utils.get(iterator, OrderedSequenceIterator.NEXT));
					if (currentFrame.size() != cfc)
						executeAll(cfc);
					ss[i] = returnee;
				}
			} catch (PythonExecutionException e){
				if (Utils.run("isinstance", e.getException(), stype).truthValue()){
					throw Utils.throwException("ValueError", "too few values to unpack");
				} else
					throw e;
			}
			
			try {
				execute(true, Utils.get(iterator, OrderedSequenceIterator.NEXT));
				if (currentFrame.size() != cfc)
					executeAll(cfc);
				throw Utils.throwException("ValueError", "too many values to unpack");
			} catch (PythonExecutionException e){
				if (!Utils.run("isinstance", e.getException(), stype).truthValue()){
					throw e;
				}
			}
			
			for (PythonObject obj : ss)
				stack.push(obj);
			
			break;
		case PUSH_LOCAL_CONTEXT:
			currentFrame.getLast().pushed_context = true;
			currentContext.add(stack.pop());
			break;
		case RESOLVE_ARGS:
			synchronized (this.args.backingMap){
				for (PythonProxy key : this.args.backingMap.keySet()){
					environment().getLocals().backingMap.put((StringObject) key.o, this.args.backingMap.get(key));
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
			runnable = environment().get(new StringObject("getattr"), true, false);
			PythonObject[] args = new PythonObject[2];
			// If argument for GETATTR is not set, attribute name is pop()ed from stack   
			if (pythonBytecode.stringValue == null) {
				args[1] = stack.pop();	// attribute
				args[0] = stack.pop();	// object
			} else {
				args[0] = stack.pop();									// object
				args[1] = new StringObject(pythonBytecode.stringValue);	// attribute
			} 
			returnee = execute(true, runnable, args);
			break;
		}
		case SETATTR: {
			runnable = environment().get(new StringObject("setattr"), true, false);
			PythonObject[] args = new PythonObject[3];
			// If argument for SETATTR is not set, attribute name is pop()ed from stack   
			if (pythonBytecode.stringValue == null) {
				args[1] = stack.pop();	// attribute
				args[0] = stack.pop();	// object
				args[2] = stack.pop();	// value
			} else {
				args[1] = new StringObject(pythonBytecode.stringValue);	// attribute
				args[0] = stack.pop();									// object
				args[2] = stack.pop();									// value
			} 
			returnee = execute(true, runnable, args);
			break;
		}
		case ISINSTANCE:
			PythonObject type = stack.pop();
			value = stack.peek();
			stack.push(BoolObject.fromBoolean(PythonRuntime.doIsInstance(value, type, true)));
			break;
		case RAISE: {
			PythonObject s;
			s = stack.pop();
			if (s == null)
				Utils.throwException("InterpretError", "no exception is being handled but raise called");
			throw new PythonExecutionException(s);
		}
		case RERAISE: {
			PythonObject s = stack.pop();
			if (s != NoneObject.NONE)
				throw new PythonExecutionException(s);
			break;
		}
		case PUSH_FRAME:
			FrameObject nf = new FrameObject();
			nf.newObject();
			nf.parentFrame = o;
			nf.bytecode = o.bytecode;
			nf.stack = o.stack;
			nf.pc = pythonBytecode.intValue;
			currentFrame.add(nf);
			break;
		case PUSH_EXCEPTION:
			frame = (FrameObject) stack.peek();
			if (frame.exception == null)
				stack.push(NoneObject.NONE);
			else
				stack.push(frame.exception);
			break;
		default:
			Utils.throwException("InterpretError", "unhandled bytecode " + pythonBytecode.getOpcode().toString());
		}

			
		return ExecutionResult.OK;
	}

	private void removeLastFrame() {
		FrameObject o = this.currentFrame.removeLast();
		if (o.pushed_context)
			currentContext.pop();
		if (o.pushed_environ)
			currentEnvironment.pop();
		if (o.parentFrame != null){
			o.parentFrame.returnHappened = o.returnHappened;
			o.parentFrame.stack.add(o);
		} else {
			if (currentFrame.size() == 0) {
				if (o.exception != null) 
					throw new PythonExecutionException(o.exception);
			} else
				currentFrame.peekLast().exception = o.exception;
		}
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

	public PythonObject executeAll(int cfc) {
		if (cfc == currentFrame.size())
			return returnee;
		while (true){
			ExecutionResult res = executeOnce();
			if (res == ExecutionResult.INTERRUPTED)
				return null;
			if (res == ExecutionResult.FINISHED || res == ExecutionResult.EOF)
				if (currentFrame.size() == cfc){
					if (exception() != null){
						PythonObject e = exception();
						currentFrame.peekLast().exception = null;
						throw new PythonExecutionException(e);
					}
					return returnee;
				}
		}
	}
}
