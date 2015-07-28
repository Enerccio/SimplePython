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
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.ModuleObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.BoolObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.base.NumberObject;
import me.enerccio.sp.types.callables.CallableObject;
import me.enerccio.sp.types.callables.UserFunctionObject;
import me.enerccio.sp.types.callables.UserMethodObject;
import me.enerccio.sp.types.iterators.XRangeIterator;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.mappings.PythonProxy;
import me.enerccio.sp.types.pointer.PointerObject;
import me.enerccio.sp.types.sequences.ListObject;
import me.enerccio.sp.types.sequences.OrderedSequenceIterator;
import me.enerccio.sp.types.sequences.SequenceObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.types.sequences.XRangeObject;
import me.enerccio.sp.utils.Utils;

@SuppressWarnings("unused")
/**
 * PythonInterpret. Interprets bytecode. One per thread and gets automatically constructed the moment something wants to access it in a thread.
 * @author Enerccio
 *
 */
public class PythonInterpret extends PythonObject {
	private static final long serialVersionUID = -8039667108607710165L;
	/** Thread local accessor to the interpret */
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
	
	/** Collection of all interprets created */
	public static final Set<PythonInterpret> interprets = Collections.synchronizedSet(new HashSet<PythonInterpret>());
	
	public PythonInterpret(){
		bind();
	}
	
	/**
	 * Binds the interpret to this thread
	 */
	public void bind(){
		interpret.set(this);
	}
	
	/** current environment stack. Topmost element is used for the current environment of the frame */
	public Stack<EnvironmentObject> currentEnvironment = new Stack<EnvironmentObject>();
	/** current context stack. Topmost element is used for resolving local context, ie whether we have access to private or not */
	public Stack<PythonObject> currentContext = new Stack<PythonObject>();
	/** current frame stack. Topmost element represents currently interpreted frame */
	public LinkedList<FrameObject> currentFrame = new LinkedList<FrameObject>();
	/** Number of times this interpret is accessed by itself. If >0, interpret can't be serialized */
	private volatile int accessCount = 0;
	/** Represents currrently passed arguments to the function */
	private MapObject args = null;
	/** Represents value returned by a call */
	private PythonObject returnee;
	
	/**
	 * whether this interpret can be stopped at safe place or not
	 * @return
	 */
	public boolean isInterpretStoppable(){
		return accessCount == 0;
	}

	/**
	 * executes single call identified by function in current environment and returns value, if any
	 * @param function
	 * @param data
	 * @return
	 */
	public PythonObject executeCall(String function, PythonObject... data) {
		if (currentEnvironment.size() == 0)
			return returnee = execute(false, PythonRuntime.runtime.generateGlobals().doGet(function), data);
		return returnee = execute(false, environment().get(new StringObject(function), false, false), data);
	}

	/**
	 * Returns current environment or null
	 * @return
	 */
	public EnvironmentObject environment() {
		return Utils.peek(currentEnvironment);
	}
	
	/**
	 * Returns current local context or null
	 * @return
	 */
	public PythonObject getLocalContext() {
		PythonObject p = Utils.peek(currentContext);
		if (p == null)
			return NoneObject.NONE;
		return p;
	}

	/**
	 * Executes call to the callable. 
	 * @param internalCall if true and call is to a python code, it will wait until that code will finish
	 * @param callable callable
	 * @param args arguments
	 * @return
	 */
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

	/**
	 * Returns exception of the topmost frame or null if either no frame is there or no exception has happened
	 * @return
	 */
	public PythonObject exception() {
		if (currentFrame.size() != 0)
			return currentFrame.peekLast().exception;
		return null;
	}

	/**
	 * invokes callable with args
	 * @param callable
	 * @param args
	 * @return
	 */
	public PythonObject invoke(PythonObject callable, TupleObject args) {
		return execute(false, callable, args.getObjects());
	}

	/**
	 * returns global value for variable key or null
	 * @param key
	 * @return
	 */
	public PythonObject getGlobal(String key) {
		return environment().get(new StringObject(key), true, false);
	}

	/**
	 * Pushes environment dicts as a new environment object
	 * @param environs
	 */
	public void pushEnvironment(MapObject... environs) {
		EnvironmentObject c;
		currentEnvironment.push(c = new EnvironmentObject());
		c.add(environs);
	}

	/**
	 * Pushes new frame on the stack that contains this bytecode.
	 * @param frame
	 */
	public void executeBytecode(List<PythonBytecode> frame) {
		FrameObject n;
		currentFrame.add(n = new FrameObject());
		n.bytecode = new ArrayList<PythonBytecode>(frame);
	}

	/**
	 * Executes single instruction
	 * @return
	 */
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

	/**
	 * Handles the exception chain.
	 * @param e
	 */
	private void handleException(PythonExecutionException e) {
		currentFrame.peekLast().exception = e.getException();
		PythonObject stack = Utils.run("getattr", exception(), new StringObject("stack"));
		if (stack instanceof ListObject){
			ListObject s = (ListObject)stack;
			s.objects.add(makeStack());
		}
		removeLastFrame();
	}

	/**
	 * Inserts current instruction into stack
	 * @return
	 */
	private StringObject makeStack() {
		return new StringObject(makeStackString());
	}

	/**
	 * Creates new stack line element of the current stack
	 * @return
	 */
	private String makeStackString() {
		FrameObject o = currentFrame.getLast();
		if (o == null)
			return "<last frame>";
		if (o.debugLine < 0)
			return "<method-call>";
		return String.format("<at module %s, line %s, char %s>", o.debugModule, o.debugLine, o.debugInLine);
	}

	/**
	 * Executes current instruction
	 * @param o current frame
	 * @param pythonBytecode current bytecode
	 * @return execution result
	 */
	private ExecutionResult executeSingleInstruction(FrameObject o,
			PythonBytecode pythonBytecode) {
		
		Stack<PythonObject> stack = o.stack;
		
		if (o.accepts_return){
			o.accepts_return = false;
			if (returnee == null)
				stack.push(NoneObject.NONE);
			else
				stack.push(returnee);
		}

		/*
		if (!o.accepts_return)
			System.out.println("<" + o.debugModule + ", " + o.debugLine + "> \t" + o.hashCode() + " \t" + Bytecode.dis(o.pc - 1, pythonBytecode) + " [" + o.stack);
		else
			System.out.println("<" + o.debugModule + ", " + o.debugLine + "> \t" + o.hashCode() + " \t" + Bytecode.dis(o.pc - 1, pythonBytecode) + " value: " + returnee  + " [" + o.stack);
			*/
		
		switch (pythonBytecode.getOpcode()){
		case NOP:
			// do nothing
			break;
		case PUSH_DICT:{
			// pushes b.mapValue into current environment
				EnvironmentObject env = Utils.peek(currentEnvironment);
				env.add(pythonBytecode.mapValue);
			} break;
		case PUSH_ENVIRONMENT:
			// pushes new environment onto environment stack. 
			// also sets flag on the current frame to later pop the environment when frame itself is popped
			currentEnvironment.push(new EnvironmentObject());
			currentFrame.getLast().pushed_environ = true;
			break;
		case CALL: {
			// calls the runnable with arguments
			// values are gathered from the stack in reverse order, with lowest being the callable itself.
			// if call(x)'s x is negative, it is vararg call, thus last argument must be a tuple that will be
			// expanded to fill the arguments
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
			o.accepts_return = true;
			break;
		}
		case SETUP_LOOP:
			// Grabs object from stack. It it is something that can be iterated internaly, pushes
			// iterator back there. Otherwise, calls iter method.
			PythonObject runnable;
			PythonObject value = stack.pop();
			if (value instanceof XRangeObject) {
				// TODO: Interface or something like that
				stack.push(((XRangeObject)value).__iter__(TupleObject.EMPTY));
				o.pc = pythonBytecode.intValue;
			} else {
				runnable = environment().get(new StringObject("iter"), true, false);				
				returnee = execute(true, runnable, value);
				o.accepts_return = true;
			}
			break;
		case ACCEPT_ITER:
			// Replaces frame left by ECALL with returnee value.
			// If StopIteration was raised, jumps to specified bytecode
			// Any other exception is rised again
			FrameObject frame = (FrameObject) stack.pop();
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
		case GET_ITER:
			value = stack.peek();
			if (value instanceof XRangeIterator) {
				value = ((XRangeIterator)value).next();
				if (value == null) {
					// StopIteration is not actually thrown, only emulated
					o.pc = pythonBytecode.intValue;
				} else {
					o.stack.push(value);
					o.pc ++; // ACCEPT_ITER _always_ follows GET_ITER and this one will skip it.
				}
				break;
			}
			// Note: Falls down to ECALL. This is not by mistake
		case ECALL:
			// Leaves called method on top of stack
			// Pushes frame in which call was called
			runnable = stack.peek();
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
		case RCALL: {
			// I have no idea what this shit is
			PythonObject[] args = new PythonObject[pythonBytecode.intValue];
			
			for (int i=0; i<args.length; i++)
				args[i] = stack.pop();
			runnable = stack.pop();
			
			returnee = execute(true, runnable, args);
			o.accepts_return = true;
			break;
		}
		case GOTO:
			// modifies the current pc to the value
			o.pc = pythonBytecode.intValue;
			break;
		case JUMPIFFALSE:
			// modifies the current pc to the value if value on stack is false
			if (!stack.pop().truthValue())
				o.pc = pythonBytecode.intValue;
			break;
		case JUMPIFTRUE:
			// modifies the current pc to the value if value on stack is true
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
			// pushes variable onto stack
			value = environment().get(new StringObject(pythonBytecode.stringValue), false, false);
			if (value == null)
				throw Utils.throwException("NameError", "name " + pythonBytecode.stringValue + " is not defined");
			stack.push(value);
			break;
		case LOADGLOBAL:
			// pushes global variable onto stack
			value = environment().get(new StringObject(pythonBytecode.stringValue), true, false);
			if (value == null)
				throw Utils.throwException("NameError", "name " + pythonBytecode.stringValue + " is not defined");
			stack.push(value);
			break;
		case POP:
			// pops value off the stack
			stack.pop();
			break;
		case TRUTH_VALUE:
			value = stack.pop();
			if (value instanceof NumberObject) {
				if (pythonBytecode.intValue == 1)
					stack.push(value.truthValue() ? BoolObject.FALSE : BoolObject.TRUE);
				else
					stack.push(value.truthValue() ? BoolObject.TRUE : BoolObject.FALSE);
				break;
			} else if (value.fields.containsKey("__nonzero__")) {
				runnable = value.fields.get("__nonzero__").object;
				returnee = execute(true, runnable);
				o.accepts_return = true;
				if (pythonBytecode.intValue == 1)
					o.pc --;
				break;
			} else if (value.fields.containsKey("__len__")) {
				runnable = value.fields.get("__len__").object;
				returnee = execute(true, runnable);
				o.accepts_return = true;
				o.pc --;
				break;
			} else {
				if (pythonBytecode.intValue == 1)
					stack.push(value.truthValue() ? BoolObject.FALSE : BoolObject.TRUE);
				else
					stack.push(value.truthValue() ? BoolObject.TRUE : BoolObject.FALSE);
			}
			break;
		case PUSH:
			// pushes constant onto stack
			stack.push(pythonBytecode.value);
			break;
		case RETURN:
			// removes the frame and returns value
			if (pythonBytecode.intValue == 1) {
				o.returnHappened = true;
				PythonObject retVal = stack.pop();
				returnee = retVal;
			}
			removeLastFrame();
			return ExecutionResult.EOF;
		case SAVE:
			// saves value into environment as variable
			environment().set(new StringObject(((Save)pythonBytecode).stringValue), stack.pop(), false, false);
			break;
		case SAVE_LOCAL:
			// saves the value exactly into locals (used by def and clas)
			environment().getLocals().backingMap.put(new StringObject(((SaveLocal)pythonBytecode).stringValue), stack.pop());
			break;
		case SAVEGLOBAL:
			// saves the value to the global variable
			environment().set(new StringObject(((Save)pythonBytecode).stringValue), stack.pop(), true, false);
			break;
		case CUSTOM:
			// executes custom bytecode
			execute(true, pythonBytecode, environment(), this, PythonRuntime.runtime.runtimeWrapper());
			break;
		case DUP:
			// duplicates stack x amount of times
			if (pythonBytecode.intValue == 0)
				stack.push(stack.peek());
			else
				stack.push(stack.get(stack.size() - 1 - pythonBytecode.intValue));
			break;
		case IMPORT:
			// import bytecode
			ModuleObject mm = (ModuleObject) 
				environment().get(new StringObject(ModuleObject.__THISMODULE__), true, false);
			String resolvePath = mm != null ? (mm.provider.getPackageResolve() != null ? mm.provider.getPackageResolve() : "") : "";
			resolvePath += resolvePath.equals("") ? pythonBytecode.stringValue2 : "." + pythonBytecode.stringValue2;
			pythonImport(environment(), pythonBytecode.stringValue, resolvePath, null);
			break;
		case SWAP_STACK: {
			// swaps head of the stack with value below it
			PythonObject top = stack.pop();
			PythonObject bot = stack.pop();
			stack.push(top);
			stack.push(bot);
			break;
		}
		case UNPACK_SEQUENCE:
			// unpacks sequence onto stack
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
			// pushes value from stack into currentContex and makrs the push into frame
			currentFrame.getLast().pushed_context = true;
			currentContext.add(stack.pop());
			break;
		case RESOLVE_ARGS:
			// resolves args into locals
			synchronized (this.args.backingMap){
				for (PythonProxy key : this.args.backingMap.keySet()){
					environment().getLocals().backingMap.put((StringObject) key.o, this.args.backingMap.get(key));
				}
			}
			break;
		case GETATTR: {
			AugumentedPythonObject apo;
			value = stack.pop();	// object to get attribute from
			apo = value.fields.get("__getattribute__"); 
			if (apo != null) {
				// There is __getattribute__ defined, call it directly
				returnee = execute(true, apo.object, new StringObject(pythonBytecode.stringValue));
				o.accepts_return = true;
				break;
			} else {
				// Try to grab argument normally...
				apo = value.fields.get(pythonBytecode.stringValue);
				if (apo != null) {
					stack.push(apo.object);
					break;
				}
				// ... and if that fails, use __getattr__ if available
				apo = value.fields.get("__getattr__"); 
				if (apo != null) {
					// There is __getattribute__ defined, call it directly
					returnee = execute(true, apo.object, new StringObject(pythonBytecode.stringValue));
					o.accepts_return = true;
					break;
				}
				throw Utils.throwException("AttributeError", "" + value.getType() + " object has no attribute '" + pythonBytecode.stringValue + "'");
			}
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
			// something stupid
			PythonObject type = stack.pop();
			value = stack.peek();
			stack.push(BoolObject.fromBoolean(PythonRuntime.doIsInstance(value, type, true)));
			break;
		case RAISE: {
			// raises python exception
			PythonObject s;
			s = stack.pop();
			if (s == null)
				throw Utils.throwException("InterpretError", "no exception is being handled but raise called");
			throw new PythonExecutionException(s);
		}
		case RERAISE: {
			PythonObject s = stack.pop();
			if (s != NoneObject.NONE)
				throw new PythonExecutionException(s);
			break;
		}
		case PUSH_FRAME:
			// inserts new subframe onto frame stack
			o.accepts_return = true;
			FrameObject nf = new FrameObject();
			nf.newObject();
			nf.parentFrame = o;
			nf.bytecode = o.bytecode;
			nf.pc = pythonBytecode.intValue;
			currentFrame.add(nf);
			break;
		case PUSH_EXCEPTION:
			// who has any idea what this shit does call 1-555-1337
			frame = (FrameObject) stack.peek();
			if (frame.exception == null)
				stack.push(NoneObject.NONE);
			else
				stack.push(frame.exception);
			break;
		default:
			Utils.throwException("InterpretError", "unhandled bytecode " + pythonBytecode.getOpcode().toString());
		}

		o.debugModule = pythonBytecode.debugModule;
		o.debugLine = pythonBytecode.debugLine;
		o.debugInLine = pythonBytecode.debugInLine;
			
		return ExecutionResult.OK;
	}

	/**
	 * Removes last frame from frame stack
	 */
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
				if (o.exception != null) {
					try {
						System.err.println(o.exception.fields.get("stack").object.toString().replace(">,", ">,\n"));
					} catch (Exception e) {};
					if (o.exception.get("__exception__", null) != null)
						throw new PythonExecutionException(o.exception, (Throwable)((PointerObject)o.exception.get("__exception__", null)).getObject());
					throw new PythonExecutionException(o.exception);
				}
			} else
				currentFrame.peekLast().exception = o.exception;
		}
	}

	/**
	 * Imports based on the values on the Import bytecode
	 * @param environment
	 * @param variable
	 * @param modulePath
	 * @param target
	 */
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

	/**
	 * Sets the arguments for the next RESOLVE_ARGS call
	 * @param a
	 */
	public void setArgs(MapObject a) {
		args = a;
	}

	/**
	 * Returns current frame
	 * @return
	 */
	public FrameObject frame() {
		return currentFrame.peekLast();
	}

	/**
	 * Executes bytecode until cfc equals the number of frames. Used to fully finish execution of newly pushed stack
	 * @param cfc
	 * @return
	 */
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
