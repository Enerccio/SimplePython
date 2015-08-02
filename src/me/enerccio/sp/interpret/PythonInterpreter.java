package me.enerccio.sp.interpret;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import me.enerccio.sp.compiler.Bytecode;
import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.compiler.PythonBytecode.*;
import me.enerccio.sp.interpret.CompiledBlockObject.DebugInformation;
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
import me.enerccio.sp.types.iterators.GeneratorObject;
import me.enerccio.sp.types.iterators.XRangeIterator;
import me.enerccio.sp.types.mappings.DictObject;
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
public class PythonInterpreter extends PythonObject {
	private static final long serialVersionUID = -8039667108607710165L;
	public static final boolean TRACE_ENABLED = System.getenv("SPY_TRACE_ENABLED") != null;
	/** Thread local accessor to the interpret */
	public static final transient ThreadLocal<PythonInterpreter> interpreter = new ThreadLocal<PythonInterpreter>(){

		@Override
		protected PythonInterpreter initialValue() {
			try {
				PythonRuntime.runtime.waitForNewInterpretAvailability();
			} catch (InterruptedException e){
				
			}
			
			PythonInterpreter i = new PythonInterpreter();
			i.newObject();
			interpreters.add(i);
			return i;
		}
		
	};
	
	/** Collection of all interprets created */
	public static final Set<PythonInterpreter> interpreters = Collections.synchronizedSet(new HashSet<PythonInterpreter>());
	
	public PythonInterpreter(){
		bind();
	}
	
	/**
	 * Binds the interpret to this thread
	 */
	public void bind(){
		interpreter.set(this);
	}
	
	/** current frame stack. Topmost element represents currently interpreted frame */
	public LinkedList<FrameObject> currentFrame = new LinkedList<FrameObject>();
	/** Number of times this interpret is accessed by itself. If >0, interpret can't be serialized */
	private volatile int accessCount = 0;
	/** Represents currrently passed arguments to the function */
	private DictObject args = null;
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
		if (currentFrame.size() == 0)
			return returnee = execute(false, PythonRuntime.runtime.getGlobals().doGet(function), null, data);
		return returnee = execute(false, environment().get(new StringObject(function), false, false), null, data);
	}

	/**
	 * Returns current environment or null
	 * @return
	 */
	public EnvironmentObject environment() {
		return currentFrame.getLast().environment;
	}
	
	/**
	 * Returns current local context or null
	 * @return
	 */
	public PythonObject getLocalContext() {
		PythonObject p = currentFrame.getLast().localContext;
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
	public PythonObject execute(boolean internalCall, PythonObject callable, KwArgs kwargs, PythonObject... args) {
		if (callable instanceof CallableObject){
			if (((callable instanceof UserFunctionObject) || (callable instanceof UserMethodObject)) && internalCall){
				int cfc = currentFrame.size();
				((CallableObject)callable).call(new TupleObject(args), kwargs);
				return executeAll(cfc);
			} else
				return returnee = ((CallableObject)callable).call(new TupleObject(args), kwargs);
		} else {
			PythonObject callableArg = callable.get(CallableObject.__CALL__, getLocalContext());
			if (callableArg == null)
				throw Utils.throwException("TypeError", callable.toString() + " is not callable");
			return returnee = execute(false, callableArg, kwargs, args);
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
	public PythonObject invoke(PythonObject callable, KwArgs kwargs, TupleObject args) {
		return execute(false, callable, kwargs, args.getObjects());
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
	 * Pushes new frame on the stack that contains this bytecode.
	 * @param frame
	 */
	public void executeBytecode(CompiledBlockObject frame) {
		FrameObject n;
		currentFrame.add(n = new FrameObject());
		n.compiled = frame;
		n.dataStream = ByteBuffer.wrap(frame.getBytedata());
		
//		System.out.println(CompiledBlockObject.dis(frame));
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
			if (o.pc >= o.compiled.getBytedata().length){
				removeLastFrame();
				returnee = null;
				return ExecutionResult.EOF;
			}
			try {
				return executeSingleInstruction(o);
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
	 * @return execution result
	 */
	private ExecutionResult executeSingleInstruction(FrameObject o) {
		int spc = o.pc;
		o.dataStream.position(spc);
		Bytecode opcode = o.nextOpcode();
		Stack<PythonObject> stack = o.stack;
		
		if (o.accepts_return){
			o.accepts_return = false;
			if (returnee == null)
				stack.push(NoneObject.NONE);
			else
				stack.push(returnee);
		}
		
		if (TRACE_ENABLED)
			System.err.println(CompiledBlockObject.dis(o.compiled, true, spc) + " " + stack);
		
		switch (opcode){
		case NOP:
			// do nothing
			break;
		case PUSH_DICT:{
			// pushes b.mapValue into current environment
				EnvironmentObject env = currentFrame.getLast().environment;
				env.add((DictObject)o.compiled.getConstant(o.nextInt()));
			} break;
		case PUSH_ENVIRONMENT:
			// pushes new environment onto environment stack. 
			// also sets flag on the current frame to later pop the environment when frame itself is popped
			o.environment = new EnvironmentObject();
			break;
		case CALL: {
			// calls the runnable with arguments
			// values are gathered from the stack in reverse order, with lowest being the callable itself.
			// if call(x)'s x is negative, it is vararg call, thus last argument must be a tuple that will be
			// expanded to fill the arguments
			int pbint = o.nextInt();
			int argl = pbint >= 0 ? pbint : -pbint;
			boolean va = false;
			if (pbint < 0)
				va = true;
			PythonObject[] args = new PythonObject[argl];
			
			for (int i=args.length-1; i>=0; i--)
				args[i] = stack.pop();
			PythonObject runnable = stack.pop();
			
			if (va){
				PythonObject[] va2 = args;
				PythonObject tp = va2[va2.length-1];
				if (!(tp instanceof TupleObject))
					// TODO: Not really...
					throw Utils.throwException("TypeError", returnee + ": last argument must be a 'tuple'");
				PythonObject[] vra = ((TupleObject)tp).getObjects();
				args = new PythonObject[va2.length - 1 + vra.length];
				for (int i=0; i<va2.length; i++)
					args[i] = va2[i];
				for (int i=0; i<vra.length; i++)
					args[i + va2.length] = va2[i];
			}
			
			returnee = execute(true, runnable, o.kwargs, args);
			o.kwargs = null;
			o.accepts_return = true;
			break;
		}
		case SETUP_LOOP:
			// Grabs object from stack. It it is something that can be iterated internally, pushes
			// iterator back there. Otherwise, calls iter method.
			PythonObject runnable;
			PythonObject value = stack.pop();
			int jv = o.nextInt();
			if (value instanceof XRangeObject) {
				// TODO: Interface or something like that
				stack.push(((XRangeObject)value).__iter__(TupleObject.EMPTY));
				o.pc = jv;
			} else {
				runnable = environment().get(new StringObject("iter"), true, false);				
				returnee = execute(true, runnable, null, value);
				o.accepts_return = true;
			}
			break;
		case ACCEPT_ITER:
			// Replaces frame left by ECALL with returnee value.
			// If StopIteration was raised, jumps to specified bytecode
			// Any other exception is rised again
			FrameObject frame = (FrameObject) stack.pop();
			jv = o.nextInt();
			if (frame.exception != null) {
				PythonObject stype = environment().get(new StringObject("StopIteration"), true, false);
				if (Utils.run("isinstance", frame.exception, stype).truthValue()) {
					o.pc = jv;
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
			jv = o.nextInt();
			value = stack.peek();
			if (value instanceof XRangeIterator) {
				value = ((XRangeIterator)value).next();
				if (value == null) {
					// StopIteration is not actually thrown, only emulated
					o.pc = jv;
				} else {
					o.stack.push(value);
					o.pc += 5; // ACCEPT_ITER _always_ follows GET_ITER and this one will skip it.
				}
				break;
			}
			// Note: Falls down to ECALL. This is not by mistake.
		case ECALL:
			// Leaves called method on top of stack
			// Pushes frame in which call was called
			runnable = stack.peek();
			try {
				returnee = execute(true, runnable, null);
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
			PythonObject[] args = new PythonObject[o.nextInt()];
			
			for (int i=0; i<args.length; i++)
				args[i] = stack.pop();
			runnable = stack.pop();
			
			returnee = execute(true, runnable, null, args);
			o.accepts_return = true;
			break;
		}
		case GOTO:
			// modifies the current pc to the value
			o.pc = o.nextInt();
			break;
		case JUMPIFFALSE:
			// modifies the current pc to the value if value on stack is false
			jv = o.nextInt();
			if (!stack.pop().truthValue())
				o.pc = jv;
			break;
		case JUMPIFTRUE:
			// modifies the current pc to the value if value on stack is true
			jv = o.nextInt();
			if (stack.pop().truthValue())
				o.pc = jv;
			break;
		case JUMPIFNONE:
			// Peeks, leaves value on stack
			jv = o.nextInt();
			if (stack.peek() == NoneObject.NONE)
				o.pc = jv;
			break;
		case JUMPIFNORETURN:
			jv = o.nextInt();
			frame = (FrameObject) stack.peek();
			if (!frame.returnHappened)
				// Frame ended without return, jump to specified label and keep frame on stack
				o.pc = jv;
			break;
		case LOAD: 
			// pushes variable onto stack
			String svl = ((StringObject)o.compiled.getConstant(o.nextInt())).value;
			value = environment().get(new StringObject(svl), false, false);
			if (value == null)
				throw Utils.throwException("NameError", "name " + svl + " is not defined");
			stack.push(value);
			break;
		case LOADGLOBAL:
			// pushes global variable onto stack
			svl = ((StringObject)o.compiled.getConstant(o.nextInt())).value;
			value = environment().get(new StringObject(svl), true, false);
			if (value == null)
				throw Utils.throwException("NameError", "name " + svl + " is not defined");
			stack.push(value);
			break;
		case POP:
			// pops value off the stack
			stack.pop();
			break;
		case TRUTH_VALUE:
			value = stack.pop();
			jv = o.nextInt();
			if (value instanceof NumberObject) {
				if (jv == 1)
					stack.push(value.truthValue() ? BoolObject.FALSE : BoolObject.TRUE);
				else
					stack.push(value.truthValue() ? BoolObject.TRUE : BoolObject.FALSE);
				break;
			} else if (value.fields.containsKey("__nonzero__")) {
				runnable = value.fields.get("__nonzero__").object;
				returnee = execute(true, runnable, null);
				o.accepts_return = true;
				if (jv == 1)
					o.pc-=5;
				break;
			} else if (value.fields.containsKey("__len__")) {
				runnable = value.fields.get("__len__").object;
				returnee = execute(true, runnable, null);
				o.accepts_return = true;
				o.pc-=5;
				break;
			} else {
				if (jv == 1)
					stack.push(value.truthValue() ? BoolObject.FALSE : BoolObject.TRUE);
				else
					stack.push(value.truthValue() ? BoolObject.TRUE : BoolObject.FALSE);
			}
			break;
		case PUSH:
			// pushes constant onto stack
			stack.push(o.compiled.getConstant(o.nextInt()));
			break;
		case RETURN:
			// removes the frame and returns value
			if (o.ownedGenerator != null && !o.yielding)
				throw Utils.throwException("StopIteration");
			if (o.nextInt() == 1) {
				o.returnHappened = true;
				PythonObject retVal = stack.pop();
				returnee = retVal;
			}
			removeLastFrame();
			o.yielding = false;
			return ExecutionResult.EOF;
		case SAVE:
			// saves value into environment as variable
			environment().set(((StringObject)o.compiled.getConstant(o.nextInt())), stack.pop(), false, false);
			break;
		case KWARG:
			// saves value into environment as variable
			if (o.kwargs == null)
				o.kwargs = new KwArgs.HashMapKWArgs();
			o.kwargs.put(o.compiled.getConstant(o.nextInt()).toString(), stack.pop());
			break;
		case SAVE_LOCAL:
			// saves the value exactly into locals (used by def and clas)
			environment().getLocals().backingMap.put(((StringObject)o.compiled.getConstant(o.nextInt())), stack.pop());
			break;
		case SAVEGLOBAL:
			// saves the value to the global variable
			environment().set(((StringObject)o.compiled.getConstant(o.nextInt())), stack.pop(), true, false);
			break;
		case DUP:
			// duplicates stack x amount of times
			jv = o.nextInt();
			if (jv == 0)
				stack.push(stack.peek());
			else
				stack.push(stack.get(stack.size() - 1 - jv));
			break;
		case IMPORT:
			// import bytecode
			String s1 = ((StringObject)o.compiled.getConstant(o.nextInt())).value;
			String s2 = ((StringObject)o.compiled.getConstant(o.nextInt())).value;
			pythonImport(environment(), s1, s2, null);
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
			PythonObject[] ss = new PythonObject[o.nextInt()];
			PythonObject stype = environment().get(new StringObject("StopIteration"), true, false);
			
			try {
				Utils.run("iter", seq);
				if (currentFrame.size() != cfc)
					executeAll(cfc);
				iterator = returnee;
				
				for (int i=ss.length-1; i>=0; i--){
					returnee = execute(true, Utils.get(iterator, OrderedSequenceIterator.NEXT), null);
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
				execute(true, Utils.get(iterator, OrderedSequenceIterator.NEXT), null);
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
			o.localContext = stack.pop();
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
			PythonObject apo;
			StringObject field = (StringObject) o.compiled.getConstant(o.nextInt());
			value = stack.pop();	// object to get attribute from
			apo = value.get("__getattribute__", getLocalContext()); 
			if (apo != null) {
				// There is __getattribute__ defined, call it directly
				returnee = execute(false, apo, null, field);
				o.accepts_return = true;
				break;
			} else {
				// Try to grab argument normally...
				apo = value.get(field.value, getLocalContext());
				if (apo != null) {
					stack.push(apo);
					break;
				}				
				// ... and if that fails, use __getattr__ if available
				apo = value.get("__getattr__", getLocalContext()); 
				if (apo != null) {
					// There is __getattribute__ defined, call it directly
					returnee = execute(false, apo, null, field);
					o.accepts_return = true;
					break;
				}
				throw Utils.throwException("AttributeError", "" + value.getType() + " object has no attribute '" + field + "'");
			}
		}
		case SETATTR: {
			runnable = environment().get(new StringObject("setattr"), true, false);
			PythonObject[] args = new PythonObject[3];
			// If argument for SETATTR is not set, attribute name is pop()ed from stack   
			PythonObject po = o.compiled.getConstant(o.nextInt());
			if (po == NoneObject.NONE) {
				args[1] = stack.pop();	// attribute
				args[0] = stack.pop();	// object
				args[2] = stack.pop();	// value
			} else {
				args[1] = new StringObject(((StringObject)po).value);	// attribute
				args[0] = stack.pop();									// object
				args[2] = stack.pop();									// value
			} 
			returnee = execute(true, runnable, null, args);
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
			nf.compiled = o.compiled;
			nf.localContext = o.localContext;
			nf.environment = o.environment;
			nf.dataStream = ByteBuffer.wrap(nf.compiled.getBytedata());
			nf.pc = o.nextInt();
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
		case YIELD:
			String name = ((StringObject) o.compiled.getConstant(o.nextInt())).value;
			if (o.ownedGenerator == null){
				List<FrameObject> ol = new ArrayList<FrameObject>();
				FrameObject oo = o;
				while (oo != null){
					ol.add(oo.cloneFrame());
					oo = oo.parentFrame;
				}
				for (int i=0; i<ol.size(); i++)
					if (i != ol.size()-1)
						ol.get(i).parentFrame = ol.get(i+1);
				Collections.reverse(ol);
				GeneratorObject generator = new GeneratorObject(name, ol);
				generator.newObject();
				for (FrameObject fr : ol)
					fr.ownedGenerator = generator;
				returnee = generator;
				o.pc -= 5;
				o.returnHappened = true;
				o.yielding = true;
				removeLastFrame();
				return ExecutionResult.EOF;
			} else {
				PythonObject sentValue = o.sendValue;
				o.sendValue = null;
				o.returnHappened = true;
				PythonObject retVal = stack.pop();
				returnee = retVal;	
				o.stack.push(sentValue);
				
				GeneratorObject generator = o.ownedGenerator;
				List<FrameObject> ol = new ArrayList<FrameObject>();
				FrameObject oo = o;
				while (oo != null){
					ol.add(oo);
					oo = oo.parentFrame;
				}
				Collections.reverse(ol);
				generator.storedFrames = ol;
				for (FrameObject fr : ol)
					fr.ownedGenerator = generator;
				
				o.returnHappened = true;
				o.yielding = true;
				removeLastFrame();
				return ExecutionResult.EOF;
			}
		case LOADDYNAMIC:{
			value = null;
			boolean found = false;
			StringObject variable = (StringObject) o.compiled.getConstant(o.nextInt());
			for (int i=currentFrame.size()-1; i>=0; i--){
				FrameObject oo = currentFrame.get(i);
				DictObject locals = oo.environment.getLocals();
				if (locals.contains(variable.value)){
					stack.push(locals.doGet(variable));
					found = true;
					break;
				}
			}
			if (!found)
				throw Utils.throwException("NameError", "dynamic variable '" + variable.value + "' is undefined");
		} break;
		case SAVEDYNAMIC: {
			value = stack.pop();
			StringObject variable = (StringObject) o.compiled.getConstant(o.nextInt());
			boolean found = false;
			for (int i=currentFrame.size()-1; i>=0; i--){
				FrameObject oo = currentFrame.get(i);
				DictObject locals = oo.environment.getLocals();
				if (locals.contains(variable.value)){
					locals.backingMap.put(variable, value);
					found = true;
					break;
				}
			}
			if (!found){
				DictObject locals = o.environment.getLocals();
				locals.backingMap.put(variable, value);
			}
		} break;
		default:
			Utils.throwException("InterpretError", "unhandled bytecode " + opcode.toString());
		}
		
		DebugInformation dd = o.compiled.getDebugInformation(spc);

		o.debugModule = dd.modulename;
		o.debugLine = dd.lineno;
		o.debugInLine = dd.charno;
			
		return ExecutionResult.OK;
	}

	/**
	 * Removes last frame from frame stack
	 */
	private void removeLastFrame() {
		FrameObject o = this.currentFrame.removeLast();
		if (o.parentFrame != null){
			o.parentFrame.returnHappened = o.returnHappened;
			o.parentFrame.yielding = o.yielding;
			o.parentFrame.sendValue = o.sendValue;
			o.yielding = false;
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
					target = PythonRuntime.runtime.getModule(variable, null);
				}
			} else if (!variable.equals("*")){
				environment.set(new StringObject(variable), 
						target,
						true, false);
			} else {
				DictObject dict = (DictObject) target.fields.get(ModuleObject.__DICT__).object;
				synchronized (dict){
					synchronized (dict.backingMap){
						for (PythonProxy key : dict.backingMap.keySet()){
							if (key.o instanceof StringObject){
								String kkey = ((StringObject)key.o).value;
								if (!kkey.startsWith("__"))
									environment.set((StringObject)key.o, 
											dict.backingMap.get(key),
											true, false);
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
				target = environment.get(new StringObject(mm), false, false);
				if (target == null || !(target instanceof ModuleObject)){
					ModuleObject thisModule = (ModuleObject) environment.get(new StringObject("__thismodule__"), true, false);
					target = null;
					synchronized (PythonRuntime.runtime){
						String resolvePath = thisModule == null ? null : thisModule.provider.getPackageResolve();
						if (resolvePath == null)
							target = PythonRuntime.runtime.root.get(mm) != null ? PythonRuntime.runtime.root.get(mm).module : null;
						if (target == null)
							target = PythonRuntime.runtime.getModule(mm, resolvePath == null ? null : new StringObject(resolvePath));
					}
				}
			} else {
				if (target instanceof ModuleObject){
					ModuleObject mod = (ModuleObject)target;
					PythonObject target2 = ((DictObject)mod.fields.get(ModuleObject.__DICT__).object).doGet(mm);
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
	public void setArgs(DictObject a) {
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

	public int getAccessCount() {
		return accessCount;
	}
}